package app.exteraless.player;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;

import java.util.ArrayList;
import java.util.WeakHashMap;

public final class PlayerSettings {

    public interface Listener {
        void onPlayerSettingsChanged();
    }

    private static final String PREFS = "exteraless_player";
    private static final String KEY_PALETTE = "palette_colors";
    private static final String KEY_BLUR = "blur_backdrop";
    private static final WeakHashMap<Listener, Boolean> listeners = new WeakHashMap<>();

    private PlayerSettings() {
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean paletteColors() {
        return prefs().getBoolean(KEY_PALETTE, true);
    }

    public static void setPaletteColors(boolean value) {
        prefs().edit().putBoolean(KEY_PALETTE, value).apply();
        notifyListeners();
    }

    public static boolean blurBackdrop() {
        return prefs().getBoolean(KEY_BLUR, true);
    }

    public static void setBlurBackdrop(boolean value) {
        prefs().edit().putBoolean(KEY_BLUR, value).apply();
        notifyListeners();
    }

    public static void addListener(Listener listener) {
        if (listener == null) {
            return;
        }
        synchronized (listeners) {
            listeners.put(listener, Boolean.TRUE);
        }
    }

    public static void removeListener(Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private static void notifyListeners() {
        AndroidUtilities.runOnUIThread(() -> {
            ArrayList<Listener> copy;
            synchronized (listeners) {
                copy = new ArrayList<>(listeners.keySet());
            }
            for (Listener listener : copy) {
                if (listener != null) {
                    listener.onPlayerSettingsChanged();
                }
            }
        });
    }
}
