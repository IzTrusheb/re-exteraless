package app.exteraless.player;

import android.text.TextUtils;

import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

public final class MusicSearchUtils {

    private MusicSearchUtils() {
    }

    public static String getBotUsername(int account) {
        String username = MessagesController.getInstance(account).config.musicSearchUsername.get();
        if (TextUtils.isEmpty(username)) {
            username = "vkmusic_bot";
        }
        if (username.startsWith("@")) {
            username = username.substring(1);
        }
        return username;
    }

    public static boolean isMusicInlineResult(TLRPC.BotInlineResult result) {
        if (result == null || !(result.document instanceof TLRPC.TL_document)) {
            return false;
        }
        return "audio".equalsIgnoreCase(result.type) || MessageObject.isMusicDocument(result.document);
    }
}
