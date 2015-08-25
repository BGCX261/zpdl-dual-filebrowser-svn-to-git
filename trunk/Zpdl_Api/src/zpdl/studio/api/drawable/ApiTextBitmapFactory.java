package zpdl.studio.api.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.FloatMath;
import android.util.LruCache;

public class ApiTextBitmapFactory {
    private LruCache<String, Bitmap> lruCache;
    private int size;
    private TextPaint paint;

    public ApiTextBitmapFactory(int maxMemory, int size) {
        lruCache = new LruCache<String, Bitmap>(maxMemory) {
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                oldValue.recycle();
            }

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return super.sizeOf(key, value);
            }
        };
        this.size = size;
        this.paint = new TextPaint();
        paint.setTextSize(size);
        paint.setTextScaleX(1);
        paint.setAntiAlias(true);
        paint.setStyle(TextPaint.Style.STROKE);
    }

    public Bitmap get(String key) {
        Bitmap bitmap = lruCache.get(key);
        if(bitmap == null) {
            int textWidth = (int) FloatMath.ceil(Layout.getDesiredWidth(key, paint));
            StaticLayout sl = new StaticLayout(key, paint, textWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);

            Bitmap.Config config = Bitmap.Config.ARGB_8888;
            bitmap = Bitmap.createBitmap(sl.getWidth(), sl.getHeight(), config);

            Canvas canvas = new Canvas(bitmap);
            bitmap.eraseColor(0);
            canvas.drawColor(0x00ffffff);
            canvas.drawText(key, 0, size, paint);

            put(key, bitmap);
        }

        return bitmap;
    }

    public void put(String key, Bitmap bitmap) {
        lruCache.put(key, bitmap);
    }

    public void remove(String key) {
        lruCache.remove(key);
    }

    public void clear() {
        lruCache.evictAll();
    }
}
