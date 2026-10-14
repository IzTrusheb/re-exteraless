package app.exteraless.player;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.FrameLayout;

import androidx.core.content.FileProvider;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.FileRefController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.Forum.ForumUtilities;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import xyz.nextalone.nagram.NaConfig;

public final class PlayerActions {

    private static final HashSet<Long> pendingProfileSaves = new HashSet<>();
    private static final HashSet<String> pendingExternalProfileSaves = new HashSet<>();

    private PlayerActions() {
    }

    public static boolean noForwards(MessageObject messageObject) {
        int account = messageObject.currentAccount;
        long dialogId = messageObject.getDialogId();
        return MessagesController.getInstance(account).isPeerNoForwards(dialogId) || messageObject.messageOwner != null && messageObject.messageOwner.noforwards;
    }

    public static boolean isProfileSavePending(MessageObject messageObject) {
        TLRPC.Document document = messageObject.getDocument();
        return document != null && pendingProfileSaves.contains(document.id);
    }

    public static boolean isSavedToProfile(MessageObject messageObject) {
        TLRPC.Document document = messageObject.getDocument();
        if (document == null) {
            return false;
        }
        MessagesController.SavedMusicIds ids = MessagesController.getInstance(messageObject.currentAccount).getSavedMusicIds();
        return ids != null && ids.ids.contains(document.id);
    }

    public static MessageObject createInlineMusicMessage(int account, TLRPC.TL_document document) {
        return createInlineMusicMessage(account, document, -1);
    }

    public static MessageObject createInlineMusicMessage(int account, TLRPC.TL_document document, int messageId) {
        TLRPC.TL_message message = new TLRPC.TL_message();
        message.out = true;
        message.id = messageId;
        long selfId = UserConfig.getInstance(account).getClientUserId();
        message.peer_id = new TLRPC.TL_peerUser();
        message.peer_id.user_id = selfId;
        message.from_id = new TLRPC.TL_peerUser();
        message.from_id.user_id = selfId;
        message.date = (int) (System.currentTimeMillis() / 1000);
        message.message = "";
        message.media = new TLRPC.TL_messageMediaDocument();
        message.media.flags |= 3;
        message.media.document = document;
        message.flags |= TLRPC.MESSAGE_FLAG_HAS_MEDIA | TLRPC.MESSAGE_FLAG_HAS_FROM_ID;
        return new MessageObject(account, message, false, true);
    }

    private static boolean silent() {
        return !NaConfig.INSTANCE.getSilentMessageByDefault().Bool();
    }

    private static void ensureAccount(LaunchActivity activity, int account) {
        if (UserConfig.selectedAccount != account) {
            activity.switchToAccount(account, true);
        }
    }

    public static void showInChat(LaunchActivity activity, MessageObject messageObject) {
        int account = messageObject.currentAccount;
        ensureAccount(activity, account);
        Bundle args = new Bundle();
        long did = messageObject.getDialogId();
        if (DialogObject.isEncryptedDialog(did)) {
            args.putInt("enc_id", DialogObject.getEncryptedChatId(did));
        } else if (DialogObject.isUserDialog(did)) {
            args.putLong("user_id", did);
        } else {
            TLRPC.Chat chat = MessagesController.getInstance(account).getChat(-did);
            if (chat != null && chat.migrated_to != null) {
                args.putLong("migrated_to", did);
                did = -chat.migrated_to.channel_id;
            }
            args.putLong("chat_id", -did);
        }
        args.putInt("message_id", messageObject.getId());
        NotificationCenter.getInstance(account).postNotificationName(NotificationCenter.closeChats);
        activity.presentFragment(new ChatActivity(args), false, false);
    }

    public static void share(LaunchActivity activity, MessageObject messageObject) {
        try {
            File f = null;
            if (!TextUtils.isEmpty(messageObject.messageOwner.attachPath)) {
                f = new File(messageObject.messageOwner.attachPath);
                if (!f.exists()) {
                    f = null;
                }
            }
            if (f == null) {
                f = FileLoader.getInstance(messageObject.currentAccount).getPathToMessage(messageObject.messageOwner);
            }
            if (f.exists()) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(messageObject.getMimeType());
                try {
                    intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(ApplicationLoader.applicationContext, ApplicationLoader.getApplicationId() + ".provider", f));
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignore) {
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                }
                activity.startActivityForResult(Intent.createChooser(intent, LocaleController.getString(R.string.ShareFile)), 500);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(LocaleController.getString(R.string.AppName));
                builder.setPositiveButton(LocaleController.getString(R.string.OK), null);
                builder.setMessage(LocaleController.getString(R.string.PleaseDownload));
                builder.show();
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static void saveToMusic(LaunchActivity activity, MessageObject messageObject, FrameLayout bulletinHost, Theme.ResourcesProvider resourcesProvider) {
        if ((Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
            return;
        }
        ArrayList<MessageObject> messages = new ArrayList<>(1);
        messages.add(messageObject);
        MediaController.saveFilesFromMessages(activity, AccountInstance.getInstance(messageObject.currentAccount), messages, copiedFiles -> {
            if (copiedFiles > 0 && bulletinHost != null) {
                BulletinFactory.of(bulletinHost, resourcesProvider).createDownloadBulletin(BulletinFactory.FileType.AUDIO).show();
            }
        });
    }

    public static void forwardTo(LaunchActivity activity, MessageObject messageObject, long dialogId) {
        int account = messageObject.currentAccount;
        ensureAccount(activity, account);
        if (messageObject.getId() < 0) {
            if (!(messageObject.getDocument() instanceof TLRPC.TL_document)) {
                return;
            }
            SendMessagesHelper.getInstance(account).sendMessage(SendMessagesHelper.SendMessageParams.of((TLRPC.TL_document) messageObject.getDocument(), null, messageObject.messageOwner.attachPath, dialogId, null, null, null, null, null, null, silent(), 0, 0, 0, MediaController.getInstance().currentSavedMusicList, null, false, false));
        } else {
            ArrayList<MessageObject> messages = new ArrayList<>();
            messages.add(messageObject);
            SendMessagesHelper.getInstance(account).sendMessage(messages, dialogId, false, false, silent(), 0, 0);
        }
        showForwardBulletin(account, dialogId, 1);
    }

    private static void showForwardBulletin(int account, long dialogId, int count) {
        BaseFragment last = LaunchActivity.getLastFragment();
        if (last == null) {
            return;
        }
        long self = UserConfig.getInstance(account).getClientUserId();
        CharSequence text;
        if (count == 1 && dialogId == self) {
            text = LocaleController.getString(R.string.FwdMessageToSavedMessages);
        } else if (count == 1 && dialogId > 0) {
            text = LocaleController.formatString(R.string.FwdMessageToUser, DialogObject.getShortName(dialogId));
        } else if (count == 1) {
            text = LocaleController.formatString(R.string.FwdMessageToGroup, DialogObject.getShortName(dialogId));
        } else {
            text = LocaleController.formatPluralStringComma("FwdMessageToManyChats", count);
        }
        BulletinFactory.of(last).createSimpleBulletin(R.raw.forward, text).show();
    }

    public static void forward(LaunchActivity activity, MessageObject messageObject) {
        int account = messageObject.currentAccount;
        ensureAccount(activity, account);
        Bundle args = new Bundle();
        args.putBoolean("onlySelect", true);
        args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
        args.putBoolean("canSelectTopics", true);
        DialogsActivity fragment = new DialogsActivity(args);
        final ArrayList<MessageObject> messages;
        final TLRPC.TL_document document;
        if (messageObject.getId() < 0) {
            messages = null;
            if (!(messageObject.getDocument() instanceof TLRPC.TL_document)) {
                return;
            }
            document = (TLRPC.TL_document) messageObject.getDocument();
        } else {
            messages = new ArrayList<>();
            messages.add(messageObject);
            document = null;
        }
        fragment.setDelegate((fragment1, dids, message, param, notify, scheduleDate, scheduleRepeatPeriod, topicsFragment) -> {
            long self = UserConfig.getInstance(account).getClientUserId();
            if (dids.size() > 1 || dids.get(0).dialogId == self || message != null || messages == null) {
                for (int a = 0; a < dids.size(); a++) {
                    long did = dids.get(a).dialogId;
                    if (message != null) {
                        SendMessagesHelper.getInstance(account).sendMessage(SendMessagesHelper.SendMessageParams.of(message.toString(), did, null, null, null, true, null, null, null, silent(), 0, 0, null, false));
                    }
                    if (messages != null) {
                        SendMessagesHelper.getInstance(account).sendMessage(messages, did, false, false, silent(), 0, 0);
                    } else {
                        SendMessagesHelper.getInstance(account).sendMessage(SendMessagesHelper.SendMessageParams.of(document, null, messageObject.messageOwner.attachPath, did, null, null, null, null, null, null, notify && silent(), scheduleDate, 0, 0, MediaController.getInstance().currentSavedMusicList, null, false, false));
                    }
                }
                fragment1.finishFragment();
                showForwardBulletin(account, dids.get(0).dialogId, dids.size());
            } else {
                MessagesStorage.TopicKey topicKey = dids.get(0);
                long did = topicKey.dialogId;
                Bundle args1 = new Bundle();
                args1.putBoolean("scrollToTopOnResume", true);
                if (DialogObject.isEncryptedDialog(did)) {
                    args1.putInt("enc_id", DialogObject.getEncryptedChatId(did));
                } else if (DialogObject.isUserDialog(did)) {
                    args1.putLong("user_id", did);
                } else {
                    args1.putLong("chat_id", -did);
                }
                ChatActivity chatActivity = new ChatActivity(args1);
                if (topicKey.topicId != 0) {
                    ForumUtilities.applyTopic(chatActivity, topicKey);
                }
                if (activity.presentFragment(chatActivity, true, false)) {
                    chatActivity.showFieldPanelForForward(true, messages);
                    if (topicKey.topicId != 0) {
                        fragment1.removeSelfFromStack();
                    }
                } else {
                    fragment1.finishFragment();
                }
            }
            return true;
        });
        activity.presentFragment(fragment);
    }

    public interface ResultCallback {
        void onResult(TLRPC.TL_error error);
    }

    public static void saveToProfile(MessageObject messageObject, boolean save, ResultCallback callback) {
        final TLRPC.Document document = messageObject.getDocument();
        if (document == null) {
            return;
        }
        if (document.id == 0 && save) {
            saveExternalTrackToProfile(messageObject, callback);
            return;
        }
        if (!pendingProfileSaves.add(document.id)) {
            return;
        }
        final long documentId = document.id;
        saveToProfile(messageObject, save, error -> {
            pendingProfileSaves.remove(documentId);
            callback.onResult(error);
        }, false);
    }

    private static void saveExternalTrackToProfile(MessageObject messageObject, ResultCallback callback) {
        final TLRPC.Document document = messageObject.getDocument();
        final String path = messageObject.messageOwner.attachPath;
        if (!(document instanceof TLRPC.TL_document) || TextUtils.isEmpty(path) || !new File(path).isFile()) {
            callback.onResult(newProfileSaveError("MUSIC_FILE_NOT_FOUND"));
            return;
        }
        final int account = messageObject.currentAccount;
        final String pendingKey = account + ":" + path;
        if (!pendingExternalProfileSaves.add(pendingKey)) {
            return;
        }
        final PendingExternalProfileSave observer = new PendingExternalProfileSave(account, pendingKey, path, document, callback);
        observer.start();
        final long selfId = UserConfig.getInstance(account).getClientUserId();
        final SendMessagesHelper.SendMessageParams params = SendMessagesHelper.SendMessageParams.of(
                (TLRPC.TL_document) document, null, path, selfId, null, null, null, null, null, null,
                silent(), 0, 0, 0, null, null, false, false);
        SendMessagesHelper.getInstance(account).sendMessage(params);
    }

    private static TLRPC.TL_error newProfileSaveError(String text) {
        TLRPC.TL_error error = new TLRPC.TL_error();
        error.code = 400;
        error.text = text;
        return error;
    }

    private static final class PendingExternalProfileSave implements NotificationCenter.NotificationCenterDelegate {
        private final int account;
        private final String pendingKey;
        private final String path;
        private final String fileName;
        private final long startedAt = System.currentTimeMillis();
        private final ResultCallback callback;
        private boolean finished;
        private int localMessageId;
        private final Runnable timeoutRunnable = () -> finish(null, newProfileSaveError("MUSIC_UPLOAD_TIMEOUT"));

        PendingExternalProfileSave(int account, String pendingKey, String path, TLRPC.Document document, ResultCallback callback) {
            this.account = account;
            this.pendingKey = pendingKey;
            this.path = path;
            this.fileName = FileLoader.getDocumentFileName(document);
            this.callback = callback;
        }

        void start() {
            NotificationCenter center = NotificationCenter.getInstance(account);
            center.addObserver(this, NotificationCenter.didReceiveNewMessages);
            center.addObserver(this, NotificationCenter.messageSendError);
            center.addObserver(this, NotificationCenter.messageReceivedByServer);
            AndroidUtilities.runOnUIThread(timeoutRunnable, 120000);
        }

        @Override
        public void didReceivedNotification(int id, int account, Object... args) {
            if (finished || this.account != account) {
                return;
            }
            if (id == NotificationCenter.didReceiveNewMessages && args.length >= 2
                    && args[0] instanceof Long dialogId
                    && dialogId == UserConfig.getInstance(account).getClientUserId()
                    && args[1] instanceof ArrayList<?> messages) {
                for (Object object : messages) {
                    if (object instanceof MessageObject messageObject
                            && TextUtils.equals(path, messageObject.messageOwner.attachPath)) {
                        localMessageId = messageObject.getId();
                        break;
                    }
                }
                return;
            }
            if (id == NotificationCenter.messageSendError && args.length > 0
                    && args[0] instanceof Integer messageId && messageId == localMessageId && localMessageId != 0) {
                deleteTemporaryProfileUpload(account, localMessageId, UserConfig.getInstance(account).getClientUserId());
                finish(null, newProfileSaveError("MUSIC_UPLOAD_FAILED"));
                return;
            }
            if (id != NotificationCenter.messageReceivedByServer || args.length < 3 || !(args[2] instanceof TLRPC.Message message)
                    || MessageObject.getDialogId(message) != UserConfig.getInstance(account).getClientUserId()
                    || message.media == null || message.media.document == null || message.media.document.id == 0) {
                return;
            }
            boolean localMessageMatch = localMessageId != 0 && args[0] instanceof Integer oldId && oldId == localMessageId;
            if (localMessageId != 0 ? !localMessageMatch : message.date * 1000L < startedAt - 30000) {
                return;
            }
            boolean matches = TextUtils.equals(path, message.attachPath)
                    || !TextUtils.isEmpty(fileName) && TextUtils.equals(fileName, FileLoader.getDocumentFileName(message.media.document));
            if (!matches) {
                return;
            }
            finish(new MessageObject(account, message, false, true), null);
        }

        private void finish(MessageObject uploadedMessage, TLRPC.TL_error error) {
            if (finished) {
                return;
            }
            finished = true;
            NotificationCenter center = NotificationCenter.getInstance(account);
            center.removeObserver(this, NotificationCenter.didReceiveNewMessages);
            center.removeObserver(this, NotificationCenter.messageSendError);
            center.removeObserver(this, NotificationCenter.messageReceivedByServer);
            AndroidUtilities.cancelRunOnUIThread(timeoutRunnable);
            pendingExternalProfileSaves.remove(pendingKey);
            if (uploadedMessage == null) {
                callback.onResult(error);
                return;
            }
            if (uploadedMessage.getDocument() != null) {
                PlayerActions.saveToProfile(uploadedMessage, true, saveError -> {
                    deleteTemporaryProfileUpload(uploadedMessage);
                    callback.onResult(saveError);
                });
            } else {
                callback.onResult(newProfileSaveError("UPLOADED_AUDIO_NOT_FOUND"));
            }
        }
    }

    private static void deleteTemporaryProfileUpload(MessageObject messageObject) {
        deleteTemporaryProfileUpload(messageObject.currentAccount, messageObject.getId(), messageObject.getDialogId());
    }

    private static void deleteTemporaryProfileUpload(int account, int messageId, long dialogId) {
        if (messageId == 0 || dialogId != UserConfig.getInstance(account).getClientUserId()) {
            return;
        }
        ArrayList<Integer> messageIds = new ArrayList<>(1);
        messageIds.add(messageId);
        AndroidUtilities.runOnUIThread(() -> MessagesController.getInstance(account).deleteMessages(
                messageIds, null, null, dialogId, 0, true, ChatActivity.MODE_DEFAULT));
    }

    private static void saveToProfile(MessageObject messageObject, boolean save, ResultCallback callback, boolean triedFileRef) {
        final int account = messageObject.currentAccount;
        final TLRPC.Document document = messageObject.getDocument();
        if (document == null) {
            return;
        }
        final long documentId = document.id;
        final TLRPC.TL_account_saveMusic req = new TLRPC.TL_account_saveMusic();
        req.unsave = !save;
        req.id = new TLRPC.TL_inputDocument();
        req.id.id = documentId;
        req.id.access_hash = document.access_hash;
        req.id.file_reference = document.file_reference != null ? document.file_reference : new byte[0];
        ConnectionsManager.getInstance(account).sendRequest(req, (res, err) -> {
            if (err != null && FileRefController.isFileRefError(err.text) && !triedFileRef && messageObject.getId() > 0) {
                refetch(messageObject, fresh -> {
                    if (fresh != null) {
                        saveToProfile(fresh, save, callback, true);
                    } else {
                        AndroidUtilities.runOnUIThread(() -> callback.onResult(err));
                    }
                });
                return;
            }
            if (err != null) {
                AndroidUtilities.runOnUIThread(() -> callback.onResult(err));
                return;
            }
            AndroidUtilities.runOnUIThread(() -> {
                MessagesController.getInstance(account).getSavedMusicIds().update(documentId, save);
                long selfId = UserConfig.getInstance(account).getClientUserId();
                TLRPC.UserFull userInfo = MessagesController.getInstance(account).getUserFull(selfId);
                if (userInfo != null) {
                    if (save) {
                        userInfo.flags2 |= TLObject.FLAG_21;
                        userInfo.saved_music = document;
                    } else if (userInfo.saved_music != null && userInfo.saved_music.id == documentId) {
                        userInfo.flags2 &= ~TLObject.FLAG_21;
                        userInfo.saved_music = null;
                    }
                    MessagesStorage.getInstance(account).updateUserInfo(userInfo, true);
                    NotificationCenter.getInstance(account).postNotificationName(NotificationCenter.profileMusicUpdated, selfId);
                }
                callback.onResult(null);
            });
        });
    }

    private interface MessageCallback {
        void onMessage(MessageObject messageObject);
    }

    private static void refetch(MessageObject messageObject, MessageCallback callback) {
        final int account = messageObject.currentAccount;
        final int msgId = messageObject.getId();
        TLObject request;
        if (messageObject.getDialogId() >= 0) {
            TLRPC.TL_messages_getMessages req = new TLRPC.TL_messages_getMessages();
            req.id.add(msgId);
            request = req;
        } else {
            TLRPC.TL_channels_getMessages req = new TLRPC.TL_channels_getMessages();
            req.channel = MessagesController.getInstance(account).getInputChannel(-messageObject.getDialogId());
            req.id.add(msgId);
            request = req;
        }
        ConnectionsManager.getInstance(account).sendRequest(request, (res, err) -> {
            MessageObject fresh = null;
            if (res instanceof TLRPC.messages_Messages) {
                TLRPC.messages_Messages r = (TLRPC.messages_Messages) res;
                for (int i = 0; i < r.messages.size(); i++) {
                    if (r.messages.get(i).id == msgId) {
                        fresh = new MessageObject(account, r.messages.get(i), false, true);
                        break;
                    }
                }
            }
            callback.onMessage(fresh);
        });
    }
}
