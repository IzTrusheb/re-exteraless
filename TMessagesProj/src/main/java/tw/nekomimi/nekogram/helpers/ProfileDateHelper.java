package tw.nekomimi.nekogram.helpers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Android adaptation of the offline Telegram ID-age estimator from
 * sepiol026-wq/telegram-id-age-research, main.py at the revision pinned in id_date.json.
 * The source project is AGPL-3.0-only; its license is bundled in app assets.
 *
 * ID-based dates are estimates, not Telegram records. Their reported error range is
 * empirical and explicitly not guaranteed by the source research.
 */
public class ProfileDateHelper {
    private static final String JSON_FILE = "id_date.json";
    private static final ArrayList<ProfileDateData> dateAnchors = new ArrayList<>();
    private static final ArrayList<ProfileDateData> uncertaintyAnchors = new ArrayList<>();
    private static final Map<Long, Long> knownDates = new HashMap<>();

    private static boolean loaded;
    private static long minimumSupportedId;
    private static long maximumSupportedId;
    private static long minimumTimestamp;

    private static void loadData() {
        synchronized (ProfileDateHelper.class) {
            if (loaded) {
                return;
            }
            loaded = true;
            try (InputStream in = ApplicationLoader.applicationContext.getAssets().open(JSON_FILE);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }

                JSONObject object = new JSONObject(out.toString("UTF-8"));
                minimumSupportedId = object.getLong("minSupportedId");
                maximumSupportedId = object.getLong("maxSupportedId");
                minimumTimestamp = object.getLong("minimumTimestamp");
                readPairs(object.getJSONArray("A"), dateAnchors);
                readPairs(object.getJSONArray("U"), uncertaintyAnchors);

                JSONArray samples = object.getJSONArray("K");
                for (int i = 0; i < samples.length(); i++) {
                    JSONArray pair = samples.getJSONArray(i);
                    knownDates.put(pair.getLong(0), pair.getLong(1));
                }
            } catch (Exception e) {
                dateAnchors.clear();
                uncertaintyAnchors.clear();
                knownDates.clear();
                FileLog.e(e);
            }
        }
    }

    private static void readPairs(JSONArray source, ArrayList<ProfileDateData> destination) throws Exception {
        for (int i = 0; i < source.length(); i++) {
            JSONArray pair = source.getJSONArray(i);
            destination.add(new ProfileDateData(pair.getLong(0), pair.getLong(1)));
        }
    }

    public static String getUserTime(Long userId) {
        return getUserTime(userId, null);
    }

    public static String getUserTime(Long userId, String registrationMonth) {
        if (userId == null || userId <= 0) {
            return LocaleController.getString(R.string.OEProfileRegistrationUnknown);
        }

        String formattedMonth = formatRegistrationMonth(registrationMonth);
        if (formattedMonth != null) {
            return LocaleController.formatString(R.string.OEProfileRegistrationMonth, formattedMonth);
        }

        loadData();
        if (dateAnchors.isEmpty() || uncertaintyAnchors.isEmpty()
                || userId < minimumSupportedId || userId > maximumSupportedId) {
            return LocaleController.getString(R.string.OEProfileRegistrationUnknown);
        }

        long now = System.currentTimeMillis() / 1000L;
        long timestamp = knownDates.containsKey(userId)
                ? knownDates.get(userId)
                : Math.round(interpolate(dateAnchors, userId));
        timestamp = Math.max(minimumTimestamp, Math.min(timestamp, now));

        int uncertaintyDays = (int) Math.ceil(interpolate(uncertaintyAnchors, userId));

        return LocaleController.formatString(
                R.string.OEProfileRegistrationEstimate,
                formatUtcDate(timestamp),
                uncertaintyDays);
    }

    private static double interpolate(ArrayList<ProfileDateData> anchors, long id) {
        if (id <= anchors.get(0).id()) {
            return anchors.get(0).value();
        }
        int last = anchors.size() - 1;
        if (id >= anchors.get(last).id()) {
            return anchors.get(last).value();
        }

        int low = 0;
        int high = last;
        while (high - low > 1) {
            int middle = (low + high) >>> 1;
            if (anchors.get(middle).id() <= id) {
                low = middle;
            } else {
                high = middle;
            }
        }

        ProfileDateData left = anchors.get(low);
        ProfileDateData right = anchors.get(high);
        double fraction = (double) (id - left.id()) / (right.id() - left.id());
        return left.value() + (right.value() - left.value()) * fraction;
    }

    private static String formatRegistrationMonth(String registrationMonth) {
        if (registrationMonth == null) {
            return null;
        }
        try {
            String[] parts = registrationMonth.split("\\.", -1);
            if (parts.length != 2) {
                return null;
            }
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            if (month < 1 || month > 12 || year < 2013 || year > 2200) {
                return null;
            }
            Calendar calendar = Calendar.getInstance(LocaleController.getInstance().getCurrentLocale());
            calendar.clear();
            calendar.set(year, month - 1, 1);
            return LocaleController.formatYearMont(calendar.getTimeInMillis() / 1000L, true);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String formatUtcDate(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return format.format(new Date(timestamp * 1000L));
    }

    private record ProfileDateData(long id, long value) {
    }
}
