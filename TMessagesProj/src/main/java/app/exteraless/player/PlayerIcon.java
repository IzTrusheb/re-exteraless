package app.exteraless.player;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.PathParser;

import org.telegram.messenger.AndroidUtilities;

import java.util.HashMap;

public class PlayerIcon extends Drawable {

    public static final String CHEVRON_DOWN = "M6 9l6 6 6-6";
    public static final String MORE = circle(12, 5, 1.8f) + circle(12, 12, 1.8f) + circle(12, 19, 1.8f);
    public static final String HEART = "M12 20.5s-7.5-4.6-7.5-10.3A4.3 4.3 0 0 1 12 7.6a4.3 4.3 0 0 1 7.5 2.6c0 5.7-7.5 10.3-7.5 10.3z";
    // Material Symbols Rounded, filled, weight 500, optical size 24 (Google Fonts, Apache-2.0).
    public static final String SHUFFLE = "M15.05 20.2Q14.6 20.2 14.288 19.887Q13.975 19.575 13.975 19.112Q13.975 18.65 14.288 18.337Q14.6 18.025 15.05 18.025H16.475L14.175 15.7Q13.825 15.375 13.838 14.925Q13.85 14.475 14.2 14.15Q14.525 13.825 14.975 13.825Q15.425 13.825 15.75 14.15L18.025 16.425V15.075Q18.025 14.6 18.338 14.287Q18.65 13.975 19.113 13.975Q19.575 13.975 19.888 14.287Q20.2 14.6 20.2 15.075V19.075Q20.2 19.55 19.875 19.875Q19.55 20.2 19.075 20.2ZM4.125 19.875Q3.825 19.575 3.825 19.112Q3.825 18.65 4.125 18.35L16.475 5.975H15.05Q14.6 5.975 14.288 5.662Q13.975 5.35 13.975 4.887Q13.975 4.425 14.288 4.112Q14.6 3.8 15.05 3.8H19.075Q19.55 3.8 19.875 4.125Q20.2 4.45 20.2 4.925V8.95Q20.2 9.4 19.888 9.712Q19.575 10.025 19.113 10.025Q18.65 10.025 18.338 9.712Q18.025 9.4 18.025 8.925V7.525L5.65 19.875Q5.35 20.175 4.888 20.188Q4.425 20.2 4.125 19.875ZM4.1 5.65Q3.8 5.35 3.8 4.887Q3.8 4.425 4.1 4.125Q4.4 3.825 4.85 3.825Q5.3 3.825 5.625 4.125L9.85 8.325Q10.175 8.625 10.175 9.075Q10.175 9.525 9.85 9.85Q9.55 10.15 9.088 10.15Q8.625 10.15 8.325 9.85Z";
    public static final String REPEAT = "M7.05 19.175 7.725 19.85Q8.05 20.175 8.038 20.613Q8.025 21.05 7.725 21.4Q7.4 21.725 6.938 21.738Q6.475 21.75 6.15 21.425L3.6 18.85Q3.425 18.7 3.35 18.487Q3.275 18.275 3.275 18.05Q3.275 17.825 3.35 17.625Q3.425 17.425 3.6 17.25L6.15 14.7Q6.475 14.375 6.938 14.387Q7.4 14.4 7.725 14.725Q8.025 15.05 8.038 15.5Q8.05 15.95 7.725 16.3L7.05 16.95H17Q17 16.95 17 16.95Q17 16.95 17 16.95V14.05Q17 13.6 17.312 13.275Q17.625 12.95 18.1 12.95Q18.575 12.95 18.888 13.275Q19.2 13.6 19.2 14.05V16.9Q19.2 17.825 18.538 18.5Q17.875 19.175 16.925 19.175ZM16.95 7.1H7Q7 7.1 7 7.1Q7 7.1 7 7.1V10Q7 10.475 6.688 10.787Q6.375 11.1 5.9 11.1Q5.425 11.1 5.113 10.787Q4.8 10.475 4.8 10V7.175Q4.8 6.225 5.463 5.562Q6.125 4.9 7.075 4.9H16.95L16.3 4.225Q15.95 3.9 15.963 3.45Q15.975 3 16.275 2.675Q16.6 2.35 17.062 2.337Q17.525 2.325 17.875 2.65L20.425 5.2Q20.575 5.375 20.65 5.575Q20.725 5.775 20.725 6Q20.725 6.225 20.65 6.425Q20.575 6.625 20.425 6.8L17.85 9.35Q17.525 9.675 17.062 9.662Q16.6 9.65 16.275 9.325Q15.975 9 15.963 8.55Q15.95 8.1 16.3 7.775Z";
    public static final String PREV = "M5.125 17.25V6.75Q5.125 6.275 5.45 5.95Q5.775 5.625 6.263 5.625Q6.75 5.625 7.075 5.95Q7.4 6.275 7.4 6.75V17.25Q7.4 17.725 7.075 18.05Q6.75 18.375 6.263 18.375Q5.775 18.375 5.45 18.05Q5.125 17.725 5.125 17.25ZM17.1 17.2 10.725 12.95Q10.475 12.775 10.35 12.525Q10.225 12.275 10.225 12Q10.225 11.725 10.35 11.475Q10.475 11.225 10.725 11.05L17.1 6.8Q17.25 6.675 17.425 6.65Q17.6 6.625 17.75 6.625Q18.2 6.625 18.538 6.938Q18.875 7.25 18.875 7.75V16.25Q18.875 16.75 18.538 17.062Q18.2 17.375 17.75 17.375Q17.6 17.375 17.425 17.35Q17.25 17.325 17.1 17.2Z";
    public static final String NEXT = "M16.6 17.25V6.75Q16.6 6.275 16.925 5.95Q17.25 5.625 17.738 5.625Q18.225 5.625 18.55 5.95Q18.875 6.275 18.875 6.75V17.25Q18.875 17.725 18.55 18.05Q18.225 18.375 17.738 18.375Q17.25 18.375 16.925 18.05Q16.6 17.725 16.6 17.25ZM5.125 16.25V7.75Q5.125 7.25 5.463 6.938Q5.8 6.625 6.25 6.625Q6.4 6.625 6.575 6.65Q6.75 6.675 6.9 6.8L13.275 11.05Q13.525 11.225 13.65 11.475Q13.775 11.725 13.775 12Q13.775 12.275 13.65 12.525Q13.525 12.775 13.275 12.95L6.9 17.2Q6.75 17.325 6.575 17.35Q6.4 17.375 6.25 17.375Q5.8 17.375 5.463 17.062Q5.125 16.75 5.125 16.25Z";
    public static final String PLAY = "M7.8 17.3V6.7Q7.8 6.225 8.137 5.9Q8.475 5.575 8.925 5.575Q9.075 5.575 9.238 5.612Q9.4 5.65 9.525 5.75L17.875 11.025Q18.125 11.2 18.25 11.462Q18.375 11.725 18.375 12Q18.375 12.275 18.25 12.537Q18.125 12.8 17.875 12.975L9.525 18.25Q9.4 18.35 9.238 18.388Q9.075 18.425 8.925 18.425Q8.475 18.425 8.137 18.1Q7.8 17.775 7.8 17.3Z";
    public static final String PAUSE = "M16.175 19.375Q15.25 19.375 14.575 18.7Q13.9 18.025 13.9 17.1V6.9Q13.9 5.975 14.575 5.3Q15.25 4.625 16.175 4.625Q17.125 4.625 17.788 5.3Q18.45 5.975 18.45 6.9V17.1Q18.45 18.025 17.788 18.7Q17.125 19.375 16.175 19.375ZM7.825 19.375Q6.875 19.375 6.213 18.7Q5.55 18.025 5.55 17.1V6.9Q5.55 5.975 6.213 5.3Q6.875 4.625 7.825 4.625Q8.75 4.625 9.425 5.3Q10.1 5.975 10.1 6.9V17.1Q10.1 18.025 9.425 18.7Q8.75 19.375 7.825 19.375Z";
    public static final String SPEED = "M12 14l3.5-3.5M4.6 18a8.5 8.5 0 1 1 14.8 0";
    public static final String LYRICS = "M4 6h12M4 11h16M4 16h9";
    public static final String QUEUE = "M4 6h12M4 11h12M4 16h7M15 14v6l5-3z";
    public static final String NOTE = "M9 17V5l11-2v12" + circle(6.5f, 17, 2.5f) + circle(17.5f, 15, 2.5f);
    public static final String CLOSE = "M6 6l12 12M18 6L6 18";

    private static final HashMap<String, Path> cache = new HashMap<>();

    private final Path strokePath;
    private final Path fillPath;
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int size;
    private boolean fillStroke;

    public PlayerIcon(String stroke, String fill, int sizeDp) {
        strokePath = stroke == null ? null : path(stroke);
        fillPath = fill == null ? null : path(fill);
        size = AndroidUtilities.dp(sizeDp);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeWidth(2f);
        fillPaint.setStyle(Paint.Style.FILL);
    }

    public static PlayerIcon stroke(String d, int sizeDp) {
        return new PlayerIcon(d, null, sizeDp);
    }

    public static PlayerIcon fill(String d, int sizeDp) {
        return new PlayerIcon(null, d, sizeDp);
    }

    public static String circle(float cx, float cy, float r) {
        return "M" + (cx - r) + " " + cy + "a" + r + " " + r + " 0 1 0 " + (2 * r) + " 0a" + r + " " + r + " 0 1 0 " + (-2 * r) + " 0z";
    }

    public static String roundRect(float x, float y, float w, float h, float r) {
        return "M" + (x + r) + " " + y + "h" + (w - 2 * r) + "a" + r + " " + r + " 0 0 1 " + r + " " + r
                + "v" + (h - 2 * r) + "a" + r + " " + r + " 0 0 1 " + (-r) + " " + r
                + "h" + (2 * r - w) + "a" + r + " " + r + " 0 0 1 " + (-r) + " " + (-r)
                + "v" + (2 * r - h) + "a" + r + " " + r + " 0 0 1 " + r + " " + (-r) + "z";
    }

    private static Path path(String d) {
        Path p = cache.get(d);
        if (p == null) {
            p = PathParser.createPathFromPathData(d);
            cache.put(d, p);
        }
        return p;
    }

    public void setColor(int color) {
        if (strokePaint.getColor() != color || fillPaint.getColor() != color) {
            strokePaint.setColor(color);
            fillPaint.setColor(color);
            invalidateSelf();
        }
    }

    public void setFillStroke(boolean fill) {
        if (fillStroke != fill) {
            fillStroke = fill;
            invalidateSelf();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect b = getBounds();
        canvas.save();
        canvas.translate(b.centerX() - size / 2f, b.centerY() - size / 2f);
        float k = size / 24f;
        canvas.scale(k, k);
        if (fillPath != null) {
            canvas.drawPath(fillPath, fillPaint);
        }
        if (strokePath != null) {
            if (fillStroke) {
                canvas.drawPath(strokePath, fillPaint);
            }
            canvas.drawPath(strokePath, strokePaint);
        }
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        int a = Math.max(0, Math.min(255, alpha));
        strokePaint.setAlpha(a);
        fillPaint.setAlpha(a);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        strokePaint.setColorFilter(colorFilter);
        fillPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return size;
    }

    @Override
    public int getIntrinsicHeight() {
        return size;
    }
}
