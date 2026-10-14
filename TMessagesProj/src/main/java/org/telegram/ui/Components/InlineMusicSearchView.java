package org.telegram.ui.Components;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import app.exteraless.player.MusicSearchUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ContextLinkCell;

import java.util.ArrayList;

public class InlineMusicSearchView extends FrameLayout {

    public interface Listener {
        void onTrackSelected(ArrayList<TLRPC.BotInlineResult> results, int position);

        void onTrackLongPressed(View anchor, TLRPC.BotInlineResult result);
    }

    private final int currentAccount = UserConfig.selectedAccount;
    private final Listener listener;
    private final RecyclerListView listView;
    private final TextView statusView;
    private final ResultAdapter adapter;
    private final LinearLayoutManager layoutManager;
    private final Runnable debounceSearch = this::requestSearch;
    private final ArrayList<TLRPC.BotInlineResult> results = new ArrayList<>();

    private String query = "";
    private String nextOffset = "";
    private String resolvedUsername;
    private TLRPC.User resolvedBot;
    private int searchGeneration;
    private int resolveRequestId;
    private int inlineRequestId;
    private boolean loading;

    public InlineMusicSearchView(Context context, Theme.ResourcesProvider resourcesProvider, Listener listener) {
        super(context);
        this.listener = listener;

        listView = new RecyclerListView(context);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context));
        listView.setVerticalScrollBarEnabled(true);
        listView.setClipToPadding(false);
        adapter = new ResultAdapter(context);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((view, position) -> {
            if (listener != null && position >= 0 && position < results.size()) {
                listener.onTrackSelected(new ArrayList<>(results), position);
            }
        });
        listView.setOnItemLongClickListener((view, position) -> {
            if (listener != null && position >= 0 && position < results.size()) {
                listener.onTrackLongPressed(view, results.get(position));
                return true;
            }
            return false;
        });
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && !loading && !TextUtils.isEmpty(nextOffset)
                        && layoutManager.findLastVisibleItemPosition() >= results.size() - 4) {
                    requestResults(searchGeneration, query, nextOffset, resolvedBot);
                }
            }
        });
        addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        statusView = new TextView(context);
        statusView.setGravity(Gravity.CENTER);
        statusView.setTextSize(15);
        statusView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourcesProvider));
        addView(statusView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));
        setStatus(R.string.InlineMusicSearchEnterQuery);
    }

    public void setPagesPadding(int top, int bottom, boolean doNotRequestLayout) {
        listView.setPadding(0, top, 0, bottom, doNotRequestLayout);
    }

    public void search(String value) {
        query = value == null ? "" : value.trim();
        searchGeneration++;
        cancelRequests();
        AndroidUtilities.cancelRunOnUIThread(debounceSearch);
        results.clear();
        nextOffset = "";
        adapter.notifyDataSetChanged();
        if (query.isEmpty()) {
            loading = false;
            setStatus(R.string.InlineMusicSearchEnterQuery);
            return;
        }
        loading = true;
        setStatus(R.string.InlineMusicSearchLoading);
        AndroidUtilities.runOnUIThread(debounceSearch, 350);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        searchGeneration++;
        AndroidUtilities.cancelRunOnUIThread(debounceSearch);
        cancelRequests();
        loading = false;
    }

    private void requestSearch() {
        if (TextUtils.isEmpty(query)) {
            return;
        }
        final int generation = searchGeneration;
        final String searchQuery = query;
        final String username = MusicSearchUtils.getBotUsername(currentAccount);
        if (resolvedBot != null && TextUtils.equals(username, resolvedUsername)) {
            requestResults(generation, searchQuery, "", resolvedBot);
            return;
        }
        TLObject cached = MessagesController.getInstance(currentAccount).getUserOrChat(username);
        if (cached instanceof TLRPC.User user && user.bot) {
            resolvedBot = user;
            resolvedUsername = username;
            requestResults(generation, searchQuery, "", user);
            return;
        }

        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = username;
        resolveRequestId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (generation != searchGeneration) {
                return;
            }
            resolveRequestId = 0;
            if (error != null || !(response instanceof TLRPC.TL_contacts_resolvedPeer resolvedPeer) || resolvedPeer.users.isEmpty()) {
                loading = false;
                setStatus(R.string.InlineMusicSearchFailed);
                return;
            }
            TLRPC.User bot = resolvedPeer.users.get(0);
            if (!bot.bot) {
                loading = false;
                setStatus(R.string.InlineMusicSearchFailed);
                return;
            }
            MessagesController.getInstance(currentAccount).putUser(bot, false);
            MessagesStorage.getInstance(currentAccount).putUsersAndChats(resolvedPeer.users, resolvedPeer.chats, true, true);
            resolvedBot = bot;
            resolvedUsername = username;
            requestResults(generation, searchQuery, "", bot);
        }));
    }

    private void requestResults(int generation, String searchQuery, String offset, TLRPC.User bot) {
        if (bot == null || generation != searchGeneration || loading && !TextUtils.isEmpty(offset)) {
            return;
        }
        loading = true;
        TLRPC.TL_messages_getInlineBotResults req = new TLRPC.TL_messages_getInlineBotResults();
        req.bot = MessagesController.getInstance(currentAccount).getInputUser(bot);
        req.peer = new TLRPC.TL_inputPeerEmpty();
        req.query = searchQuery;
        req.offset = offset;
        inlineRequestId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (generation != searchGeneration) {
                return;
            }
            inlineRequestId = 0;
            loading = false;
            if (error != null || !(response instanceof TLRPC.messages_BotResults botResults)) {
                if (results.isEmpty()) {
                    setStatus(R.string.InlineMusicSearchFailed);
                }
                return;
            }
            if (!botResults.users.isEmpty()) {
                MessagesController.getInstance(currentAccount).putUsers(botResults.users, false);
                MessagesStorage.getInstance(currentAccount).putUsersAndChats(botResults.users, null, true, true);
            }
            ArrayList<TLRPC.BotInlineResult> musicResults = new ArrayList<>();
            for (TLRPC.BotInlineResult result : botResults.results) {
                if (MusicSearchUtils.isMusicInlineResult(result)) {
                    result.query_id = botResults.query_id;
                    musicResults.add(result);
                }
            }
            if (TextUtils.isEmpty(offset)) {
                results.clear();
            }
            results.addAll(musicResults);
            nextOffset = botResults.next_offset;
            adapter.notifyDataSetChanged();
            if (results.isEmpty()) {
                setStatus(R.string.InlineMusicSearchNoResults);
            } else {
                statusView.setVisibility(GONE);
                listView.setVisibility(VISIBLE);
            }
        }));
    }

    private void cancelRequests() {
        if (resolveRequestId != 0) {
            ConnectionsManager.getInstance(currentAccount).cancelRequest(resolveRequestId, true);
            resolveRequestId = 0;
        }
        if (inlineRequestId != 0) {
            ConnectionsManager.getInstance(currentAccount).cancelRequest(inlineRequestId, true);
            inlineRequestId = 0;
        }
    }

    private void setStatus(int stringRes) {
        listView.setVisibility(GONE);
        statusView.setText(stringRes);
        statusView.setVisibility(VISIBLE);
    }

    private final class ResultAdapter extends RecyclerListView.SelectionAdapter {
        private final Context context;

        ResultAdapter(Context context) {
            this.context = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerListView.Holder(new ContextLinkCell(context));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((ContextLinkCell) holder.itemView).setLink(results.get(position), resolvedBot, false, position != results.size() - 1, false);
        }
    }
}
