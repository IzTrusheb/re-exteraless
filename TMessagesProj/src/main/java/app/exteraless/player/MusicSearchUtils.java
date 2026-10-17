package app.exteraless.player;

import android.text.TextUtils;

import app.exteraless.general.GeneralConfig;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

public final class MusicSearchUtils {

    private static final String DEFAULT_MUSIC_SEARCH_PREFIX = "#мч";

    private MusicSearchUtils() {
    }

    public static boolean isMusicSearchEnabled() {
        return GeneralConfig.isMusicSearchEnabled();
    }

    public static void setMusicSearchEnabled(boolean enabled) {
        GeneralConfig.setMusicSearchEnabled(enabled);
    }

    public static String getMusicSearchAlias() {
        String value = normalizeMusicSearchAlias(GeneralConfig.musicSearchAlias());
        return value == null ? "" : value;
    }

    public static void setMusicSearchAlias(String alias) {
        String normalized = normalizeMusicSearchAlias(alias);
        if (normalized != null) {
            GeneralConfig.setMusicSearchAlias(normalized);
        }
    }

    public static String normalizeMusicSearchAlias(String alias) {
        if (alias == null) {
            return null;
        }
        String value = alias.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        if (value.isEmpty()) {
            return "";
        }
        if (value.codePointCount(0, value.length()) > 32) {
            return null;
        }
        for (int offset = 0; offset < value.length();) {
            int codePoint = value.codePointAt(offset);
            if (!Character.isLetterOrDigit(codePoint) && codePoint != '_') {
                return null;
            }
            offset += Character.charCount(codePoint);
        }
        return "#" + value;
    }

    public static String getMusicSearchQuery(String text) {
        return text == null ? null : getMusicSearchQueryForUntrimmedText(text.trim());
    }

    public static String getMusicSearchQueryAtCursor(String text) {
        return getMusicSearchQueryForUntrimmedText(text);
    }

    private static String getMusicSearchQueryForUntrimmedText(String text) {
        String query = getQueryForPrefix(text, DEFAULT_MUSIC_SEARCH_PREFIX);
        if (query != null) {
            return query;
        }
        String alias = getMusicSearchAlias();
        if (!alias.isEmpty() && !alias.equalsIgnoreCase(DEFAULT_MUSIC_SEARCH_PREFIX)) {
            return getQueryForPrefix(text, alias);
        }
        return null;
    }

    private static String getQueryForPrefix(String text, String prefix) {
        if (text == null || text.length() < prefix.length()
                || !text.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return null;
        }
        if (text.length() > prefix.length() && !Character.isWhitespace(text.charAt(prefix.length()))) {
            return null;
        }
        return text.substring(prefix.length()).trim();
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
