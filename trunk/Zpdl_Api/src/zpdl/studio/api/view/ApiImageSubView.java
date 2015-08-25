package zpdl.studio.api.view;

import zpdl.studio.api.util.ApiLog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ApiImageSubView extends View {
    public static final int None = 0;

    private Context context;

    private int     FrameWidth;
    private int     FrameHeight;

    private Bitmap  MainBm;
    private Rect    MainBound;
    private int     MainWidth;
    private int     MainHegiht;

    private Bitmap  SubBm;
    private Rect    SubBound;
    private int     SubWidth;
    private int     SubHegiht;
    private int     SubId;

    private int     mLeft;
    private int     mTop;
    private int     mRight;
    private int     mBottom;

    private Paint   Paint = new Paint();
    private boolean LayoutChanged;

    public ApiImageSubView(Context context) {
        this(context, null);
    }

    public ApiImageSubView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ApiImageSubView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.context = context;
        _init(attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width  = getMeasureSize(widthMeasureSpec, FrameWidth);
        int height = getMeasureSize(heightMeasureSpec, FrameHeight);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed) {
            mLeft = left; mTop = top; mRight = right; mBottom = bottom;
            LayoutChanged = true;
        }

        if(LayoutChanged) {
            if(MainBm != null) {
                if(SubBm != null) {
                    int dstLeft = (FrameWidth  - MainWidth) / 2;
                    int dstTop  = (FrameHeight - MainHegiht) / 2;
                    MainBound.set(dstLeft, dstTop, dstLeft + MainWidth, dstTop + MainHegiht);

                    dstLeft = right -  left - SubWidth;
                    dstTop  = bottom - top  - SubHegiht;
                    SubBound.set(dstLeft, dstTop, dstLeft + SubWidth, dstTop + SubHegiht);
                } else {
                    int dstLeft = (FrameWidth  - MainWidth ) / 2;
                    int dstTop  = (FrameHeight - MainHegiht) / 2;
                    MainBound.set(dstLeft, dstTop, dstLeft + MainWidth, dstTop + MainHegiht);
                }
            }

            LayoutChanged = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(MainBm != null) {
            canvas.drawBitmap(MainBm, null, MainBound, Paint);
        }

        if(SubBm != null) {
            canvas.drawBitmap(SubBm, null, SubBound, Paint);
        }
    }

    /* Note
     * This parametar is Main Bitmap size
     * caculate - frame , main , sub
     */
    public void setSize(int w, int h, int sw, int sh) {
        MainWidth   = w;
        MainHegiht  = h;
        SubWidth    = sw;
        SubHegiht   = sh;
        FrameWidth  = MainWidth + SubWidth / 2;
        FrameHeight = MainHegiht + SubHegiht / 2;
    }

    public void setSubIconId(int id) {
        if(SubId == id) {
            return;
        }
        SubId = id;

        if(SubBm != null) {
            SubBm.recycle();
            SubBm = null;
        }

        if(id == None) {
            return;
        } else {
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
            if (bitmap == null) {
                ApiLog.e("setSubIconId <bitmap> is null");
            } else {
                SubBm = bitmap;
                LayoutChanged = true;
                onLayout(false, mLeft, mTop, mRight, mBottom);
                invalidate();
            }
        }
    }

    public void setMainIconId(int id) {
        if(MainBm != null) {
            MainBm.recycle();
            MainBm = null;
        }

        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        if (bitmap == null) {
            ApiLog.e("setMainIconId <bitmap> is null");
        } else {
            MainBm = bitmap;
            MainWidth  = MainBm.getWidth();
            MainHegiht = MainBm.getHeight();
            LayoutChanged = true;
            onLayout(false, mLeft, mTop, mRight, mBottom);
            invalidate();
        }
    }

    public void setMainIconBMP(Bitmap b) {
        if(MainBm != null) {
            MainBm.recycle();
            MainBm = null;
        }

        if (b == null) {
            ApiLog.e("setMainIconBMP <bitmap> is null");
        } else {
            MainBm = b.copy(Bitmap.Config.ARGB_8888,true);
            MainWidth  = MainBm.getWidth();
            MainHegiht = MainBm.getHeight();
            LayoutChanged = true;
            onLayout(false, mLeft, mTop, mRight, mBottom);
            invalidate();
        }
    }

    public int getMainWidth() {
        return MainWidth;
    }

    public int getMainHeight() {
        return MainHegiht;
    }

    private void _init(AttributeSet attrs, int defStyle) {
        mLeft       = 0;
        mTop        = 0;
        mRight      = 0;
        mBottom     = 0;

        FrameWidth  = 0;
        FrameHeight = 0;

        MainWidth   = 0;
        MainHegiht  = 0;
        SubWidth    = 0;
        SubHegiht   = 0;

        MainBm = null;
        MainBound = new Rect();

        SubBm = null;
        SubBound = new Rect();
        SubId = None;

        Paint = new Paint();
        LayoutChanged = true;

        int[] ids = new int[attrs.getAttributeCount()];
        for(int i = 0; i < attrs.getAttributeCount(); i++) {
            ids[i] = attrs.getAttributeNameResource(i);
        }

        TypedArray a = context.obtainStyledAttributes(attrs, ids, defStyle, 0);

        for(int i = 0; i < attrs.getAttributeCount(); i++) {
            String attrName = attrs.getAttributeName(i);
            if (attrName == null)
                continue;
        }

        a.recycle();
    }

    private int getMeasureSize(int measureSpec, int cSize) {
        int measureMode = MeasureSpec.getMode(measureSpec);
        int measureSize = MeasureSpec.getSize(measureSpec);
        int size = cSize;

        if(measureMode == MeasureSpec.EXACTLY){
            size = measureSize;
        } else if(measureMode == MeasureSpec.UNSPECIFIED || measureMode == MeasureSpec.AT_MOST){
            // Check against our minimum width
            size = Math.max(size, getSuggestedMinimumWidth());

            if (measureMode == MeasureSpec.AT_MOST) {
                size = Math.min(measureSize, size);
            }
        }

        return size;
    }
}
