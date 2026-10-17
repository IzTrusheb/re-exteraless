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
    private static final String KEY_BUTTON_SHAPE = "button_shape";
    private static final String KEY_BUTTON_SHAPE_FORMAT = "button_shape_format";
    private static final int BUTTON_SHAPE_FORMAT_VERSION = 2;

    public static final int BUTTON_SHAPE_CLASSIC = 0;
    public static final int BUTTON_SHAPE_M3_CIRCLE = 3;
    public static final int BUTTON_SHAPE_M3_SLANTED = 4;
    public static final int BUTTON_SHAPE_M3_ARCH = 5;
    public static final int BUTTON_SHAPE_M3_FAN = 6;
    public static final int BUTTON_SHAPE_M3_ARROW = 7;
    public static final int BUTTON_SHAPE_M3_SEMICIRCLE = 8;
    public static final int BUTTON_SHAPE_M3_OVAL = 9;
    public static final int BUTTON_SHAPE_M3_PILL = 10;
    public static final int BUTTON_SHAPE_M3_TRIANGLE = 11;
    public static final int BUTTON_SHAPE_M3_DIAMOND = 12;
    public static final int BUTTON_SHAPE_M3_CLAMSHELL = 13;
    public static final int BUTTON_SHAPE_M3_PENTAGON = 14;
    public static final int BUTTON_SHAPE_M3_GEM = 15;
    public static final int BUTTON_SHAPE_M3_VERY_SUNNY = 16;
    public static final int BUTTON_SHAPE_M3_SUNNY = 17;
    public static final int BUTTON_SHAPE_M3_COOKIE_4 = 18;
    public static final int BUTTON_SHAPE_M3_COOKIE_6 = 19;
    public static final int BUTTON_SHAPE_M3_COOKIE_7 = 20;
    public static final int BUTTON_SHAPE_M3_COOKIE_9 = 21;
    public static final int BUTTON_SHAPE_M3_COOKIE_12 = 22;
    public static final int BUTTON_SHAPE_M3_GHOSTISH = 23;
    public static final int BUTTON_SHAPE_M3_CLOVER_4 = 24;
    public static final int BUTTON_SHAPE_M3_CLOVER_8 = 25;
    public static final int BUTTON_SHAPE_M3_BURST = 26;
    public static final int BUTTON_SHAPE_M3_SOFT_BURST = 27;
    public static final int BUTTON_SHAPE_M3_BOOM = 28;
    public static final int BUTTON_SHAPE_M3_SOFT_BOOM = 29;
    public static final int BUTTON_SHAPE_M3_FLOWER = 30;
    public static final int BUTTON_SHAPE_M3_PUFFY = 31;
    public static final int BUTTON_SHAPE_M3_PUFFY_DIAMOND = 32;
    public static final int BUTTON_SHAPE_M3_PIXEL_CIRCLE = 33;
    public static final int BUTTON_SHAPE_M3_PIXEL_TRIANGLE = 34;
    public static final int BUTTON_SHAPE_M3_BUN = 35;
    public static final int BUTTON_SHAPE_M3_HEART = 36;
    public static final int BUTTON_SHAPE_DEFAULT = BUTTON_SHAPE_M3_CIRCLE;
    public static final int BUTTON_SHAPE_MAX = BUTTON_SHAPE_M3_HEART;

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

    public static int m3Shape() {
        SharedPreferences preferences = prefs();
        migrateButtonShape(preferences);
        int shape = preferences.getInt(KEY_BUTTON_SHAPE, BUTTON_SHAPE_DEFAULT);
        return isValidButtonShape(shape)
                ? shape : BUTTON_SHAPE_DEFAULT;
    }

    public static void setM3Shape(int shape) {
        SharedPreferences preferences = prefs();
        migrateButtonShape(preferences);
        if (!isValidButtonShape(shape)) {
            return;
        }
        preferences.edit().putInt(KEY_BUTTON_SHAPE, shape).apply();
    }

    static boolean isValidButtonShape(int shape) {
        return shape == BUTTON_SHAPE_CLASSIC
                || shape >= BUTTON_SHAPE_M3_CIRCLE && shape <= BUTTON_SHAPE_MAX;
    }

    private static void migrateButtonShape(SharedPreferences preferences) {
        int format = preferences.getInt(KEY_BUTTON_SHAPE_FORMAT, 0);
        if (format < BUTTON_SHAPE_FORMAT_VERSION) {
            int oldShape = preferences.getInt(KEY_BUTTON_SHAPE, BUTTON_SHAPE_DEFAULT);
            SharedPreferences.Editor editor = preferences.edit()
                    .putInt(KEY_BUTTON_SHAPE_FORMAT, BUTTON_SHAPE_FORMAT_VERSION);
            if (format == 0 || oldShape < BUTTON_SHAPE_M3_CIRCLE || oldShape > BUTTON_SHAPE_MAX) {
                editor.putInt(KEY_BUTTON_SHAPE, BUTTON_SHAPE_DEFAULT);
            }
            editor.apply();
        }
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
