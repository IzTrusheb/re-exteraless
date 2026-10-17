package app.exteraless.player;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;

final class PlayerShapePreviewDrawable extends Drawable {

    private final int shape;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int alpha = 255;

    PlayerShapePreviewDrawable(int shape) {
        this.shape = shape;
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            return;
        }
        int oldAlpha = paint.getAlpha();
        paint.setAlpha(alpha);
        canvas.save();
        canvas.translate(bounds.left, bounds.top);
        canvas.scale(bounds.width(), bounds.height());
        if (shape == PlayerSettings.BUTTON_SHAPE_CLASSIC) {
            canvas.drawRoundRect(0, 0, 1, 1, 0.32f, 0.32f, paint);
        } else {
            canvas.drawPath(Material3ExpressiveShapes.pathFor(shape), paint);
        }
        canvas.restore();
        paint.setAlpha(oldAlpha);
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return AndroidUtilities.dp(24);
    }

    @Override
    public int getIntrinsicHeight() {
        return AndroidUtilities.dp(24);
    }
}
