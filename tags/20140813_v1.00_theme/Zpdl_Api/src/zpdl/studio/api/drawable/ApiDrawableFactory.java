package zpdl.studio.api.drawable;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

public class ApiDrawableFactory {

    public static Bitmap getBitmap(Resources r, ApiDrawableConfig config, int id) {
        TypedValue value = new TypedValue();
        r.getValueForDensity(id, config.densityDpi, value, true);

        Bitmap bm = null;
        try {
            String file = value.string.toString();
            AssetFileDescriptor afd = r.getAssets().openNonAssetFd(value.assetCookie, file);
            bm = BitmapFactory.decodeStream(afd.createInputStream());

            if(config.ratio != 1) {
                int width = (int) (bm.getWidth() * config.ratio);
                int height = (int) (bm.getHeight() * config.ratio);
                bm = Bitmap.createScaledBitmap(bm, width, height, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }

    public static Bitmap getBitmap(Resources r, ApiDrawableConfig config, int id, int width, int height) {
        TypedValue value = new TypedValue();
        r.getValueForDensity(id, config.densityDpi, value, true);

        Bitmap bm = null;
        try {
            String file = value.string.toString();
            AssetFileDescriptor afd = r.getAssets().openNonAssetFd(value.assetCookie, file);
            bm = BitmapFactory.decodeStream(afd.createInputStream());

            if(width != bm.getWidth() || height != bm.getHeight()) {
                bm = Bitmap.createScaledBitmap(bm, width, height, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }

    public static Drawable getDrawable(Resources res, Bitmap bm) {
        BitmapDrawable drawable  = new BitmapDrawable(res, bm);

        return drawable;
    }

    public static Drawable getDrawable(Resources res, Bitmap enable, Bitmap press, Bitmap disable) {
        BitmapDrawable dEnable  = new BitmapDrawable(res, enable);
        BitmapDrawable dPress   = new BitmapDrawable(res, press);
        BitmapDrawable dDisable = new BitmapDrawable(res, disable);

        StateListDrawable  d = new StateListDrawable();

        d.addState(new int[] { -android.R.attr.state_enabled }, dDisable);
        d.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_pressed }, dPress);
        d.addState(new int[] { android.R.attr.state_enabled }, dEnable);

        return d;
    }

//    public static Drawable crateBtnDrawable(Context c) {
//        StateListDrawable  d = new StateListDrawable();
//
//        d.addState(new int[] { -android.R.attr.state_enabled },
//                    btnDisable(c));
//        d.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_pressed },
//                    btnPress(c));
//        d.addState(new int[] { android.R.attr.state_enabled },
//                    btnEnable(c));
//        return d;
//    }
//
//    private static ShapeDrawable btnEnable(Context c) {
//        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, c.getResources().getDisplayMetrics());
//
//        LinearGradient lg = new LinearGradient(0, 0, 0, 270,
//                new int[] { 0xfff1f1f2, 0xffe7e7e8, 0xfff1f1f2 },
//                null, Shader.TileMode.MIRROR);
//
//        float radius = 6 * dip;
//        float[] outerR = new float[] { radius, radius, radius, radius, radius, radius, radius, radius };
//        KhShapeDrawable btnBg = new KhShapeDrawable(new RoundRectShape(outerR, null, null));
//        btnBg.getPaint().setShader(lg);
//
//        btnBg.getStrokePaint().setStrokeWidth(1 * dip);
//        btnBg.getStrokePaint().setColor(0xfff5f5f5);
//        return btnBg;
//    }
//
//    private static ShapeDrawable btnPress(Context c) {
//        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, c.getResources().getDisplayMetrics());
//
//        LinearGradient lg = new LinearGradient(0, 0, 270, 270,
//                new int[] { 0xff18d7e5, 0xff10d7e5, 0xff18d7e5 },
//                null, Shader.TileMode.MIRROR);
//
//        float radius = 6 * dip;
//        float[] outerR = new float[] { radius, radius, radius, radius, radius, radius, radius, radius };
//        KhShapeDrawable btnBg = new KhShapeDrawable(new RoundRectShape(outerR, null, null));
//        btnBg.getPaint().setShader(lg);
//
//        btnBg.getStrokePaint().setStrokeWidth(1 * dip);
//        btnBg.getStrokePaint().setColor(0xfff5f5f5);
//        return btnBg;
//    }
//
//    private static ShapeDrawable btnDisable(Context c) {
//        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, c.getResources().getDisplayMetrics());
//
//        LinearGradient lg = new LinearGradient(0, 0, 270, 270,
//                new int[] { 0x78000000, 0x78000000, 0x78000000 },
//                null, Shader.TileMode.MIRROR);
//
//        float radius = 6 * dip;
//        float[] outerR = new float[] { radius, radius, radius, radius, radius, radius, radius, radius };
//        KhShapeDrawable btnBg = new KhShapeDrawable(new RoundRectShape(outerR, null, null));
//        btnBg.getPaint().setShader(lg);
//
//        btnBg.getStrokePaint().setStrokeWidth(1 * dip);
//        btnBg.getStrokePaint().setColor(0xfff5f5f5);
//        return btnBg;
//    }
}
