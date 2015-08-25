package zpdl.studio.api.drawable;

import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class ApiBitmapFactory {
    public static Bitmap decodeResource(Resources res, int id) {
        return BitmapFactory.decodeResource(res, id);
    }

    public static Bitmap createBitmap(Resources res, int id, int width, int height) {
        return Bitmap.createScaledBitmap(decodeResource(res, id), width, height, false);
    }

    public static Bitmap createBitmap(String filename) {
        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeFile(filename);
        } catch(OutOfMemoryError e) {
            boolean finish = true;
            int inSampleSize = 1;
            BitmapFactory.Options options = new BitmapFactory.Options();

            while(finish) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filename, options );

                try{
                    inSampleSize++;
                    options.inSampleSize = inSampleSize;
                    options.inJustDecodeBounds = false;

                    bitmap = BitmapFactory.decodeFile(filename, options);
                    finish = false;
                } catch(OutOfMemoryError e1) {
                    Log.w("KhBitmapFactory",String.format("createBitmap OutOfMemoryError w = %d , h = %d , inSampleSize = %d", options.outWidth, options.outHeight , inSampleSize));
                }
            }
        }

        return bitmap;
    }

    public static Bitmap createBitmapOnRatio(String filename, int width, int height ) {
        Bitmap bitmap = createBitmap(filename);
        Bitmap b = null;

        if ( bitmap != null ) {
            b = createBitmapOnRatio(bitmap, width, height);
        }

        return b;
    }

    public static Bitmap createBitmapOnRatioFast(String filename, int width, int height ) {
        int inSampleSize = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        double ratioWidth  = (double) options.outWidth  / (double) width;
        double ratioHeight = (double) options.outHeight / (double) height;

        if(ratioWidth > ratioHeight) {
            inSampleSize = (int) ratioWidth;
        } else {
            inSampleSize = (int) ratioHeight;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        Bitmap bm = BitmapFactory.decodeFile(filename, options);
        Bitmap result = null;
        if(bm != null) {
            if(bm.getWidth() != width && bm.getHeight() != height) {
                result = createBitmapOnRatio(bm, width, height);
            } else {
                result = bm;
            }
        }

        return result;
    }

    public static Bitmap createBitmapOnRatio(Bitmap bitmap, int width, int height) {
        int modifyWidth = width;
        int modifyHeight = height;
        Bitmap bm = null;

        if(bitmap != null) {
            if(bitmap.getWidth() >= bitmap.getHeight()) {
                modifyHeight = (int) ((double) modifyWidth * (double) bitmap.getHeight() / (double) bitmap.getWidth());
            } else {
                modifyWidth = (int) ((double) modifyHeight * (double) bitmap.getWidth() / (double) bitmap.getHeight());
            }
            if(bitmap.getWidth() == modifyWidth && bitmap.getHeight() == modifyHeight) {
                bm = bitmap.copy(Bitmap.Config.ARGB_8888,true);
            } else {
                bm = Bitmap.createScaledBitmap(bitmap, modifyWidth, modifyHeight, false);
            }
        }
        bitmap.recycle();

        return bm;
    }

    public static Bitmap createBitmapOnFit(Bitmap bitmap, int width, int height) {
        Bitmap bm = null;

        if(bitmap.getWidth() == width && bitmap.getHeight() == height) {
            bm = bitmap;
        } else {
            bm = Bitmap.createScaledBitmap(bitmap, width, height, false);
            bitmap.recycle();
        }
        return bm;
    }

    public static Bitmap createBitmapOnRectFast(String filename, int width, int height ) {
        int inSampleSize = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();

        int degree = 0;
        try {
            ExifInterface exif = new ExifInterface(filename);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        double ratioWidth  = (double) options.outWidth  / (double) width;
        double ratioHeight = (double) options.outHeight / (double) height;

        if(ratioWidth > ratioHeight) {
            inSampleSize = (int) ratioHeight;
        } else {
            inSampleSize = (int) ratioWidth;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        Bitmap bm = BitmapFactory.decodeFile(filename, options);
        Bitmap result = null;
        if(bm != null) {
            if(bm.getWidth() != width && bm.getHeight() != height) {
                result = createBitmapOnRect(bm, width, height, degree);
            } else {
                result = bm;
            }
        }

        return result;
    }

    public static Bitmap createBitmapOnRect(Bitmap bitmap, int width, int height, float degrees) {
        int modifyWidth = width;
        int modifyHeight = height;
        int x = 0;
        int y = 0;
        Bitmap bm_scale = null;
        Bitmap bm_rect = null;

        int rect_width = 0;
        int rect_height = 0;
        float rect_ratio = 0;
        if(bitmap != null) {
            if(degrees == 90 || degrees == 270) {
                modifyWidth = height;
                modifyHeight = width;
            }

            float ratioWidth = (float) bitmap.getWidth() / modifyWidth;
            float ratioHeight = (float) bitmap.getHeight() / modifyHeight;;
            if(ratioWidth > ratioHeight) {
                rect_ratio = (float) modifyHeight / bitmap.getHeight();
                rect_width = Math.round(modifyWidth * ratioHeight);
                rect_height = Math.round(modifyHeight * ratioHeight);
                x = (bitmap.getWidth() - rect_height) / 2;
                y = 0;
            } else {
                rect_ratio = (float) modifyWidth / bitmap.getWidth();;
                rect_width = Math.round(modifyWidth * ratioWidth);
                rect_height = Math.round(modifyHeight * ratioWidth);
                x = 0;
                y = (bitmap.getHeight() - rect_height) / 2;
            }
            Matrix matrix = new Matrix();
            matrix.postScale(rect_ratio, rect_ratio);
            bm_scale = Bitmap.createBitmap(bitmap, x, y, rect_width, rect_height, matrix, false);
            bitmap.recycle();

            if(degrees == 0) {
                bm_rect = bm_scale;
            } else {
                matrix.setRotate(degrees);
                bm_rect = Bitmap.createBitmap(bm_scale, 0, 0, modifyWidth, modifyHeight, matrix, false);
                bm_scale.recycle();
            }
        }

        return bm_rect;
    }

    public static ApiThumbnailParam createThumbnailOnHeight(String filename, int height) {
        int degree = 0;
        try {
            ExifInterface exif = new ExifInterface(filename);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        options.inSampleSize = (int) ((double) options.outHeight / (double) height);
        options.inJustDecodeBounds = false;

        Bitmap bm = BitmapFactory.decodeFile(filename, options);

        return new ApiThumbnailParam(createBitmapOnHeight(bm, height, degree),
                                     options.outWidth,
                                     options.outHeight);
    }

    public static Bitmap createBitmapOnHeight(Bitmap bitmap, int height, float degrees) {
        if(bitmap == null)  return null;

        float ratio = 0;

        if(degrees == 90 || degrees == 270) {
            ratio = (float) height / bitmap.getWidth();
        } else {
            ratio = (float) height / bitmap.getHeight();
        }

        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        Bitmap bm_scale = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        bitmap.recycle();

        Bitmap bm_degress = null;
        if(degrees == 0) {
            bm_degress = bm_scale;
        } else {
          matrix.setRotate(degrees);
          bm_degress = Bitmap.createBitmap(bm_scale, 0, 0, bm_scale.getWidth(), bm_scale.getHeight(), matrix, false);
          bm_scale.recycle();
        }
        return bm_degress;
    }
}
