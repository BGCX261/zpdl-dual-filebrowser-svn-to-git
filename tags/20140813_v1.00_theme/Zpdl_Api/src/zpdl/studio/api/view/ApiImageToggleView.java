package zpdl.studio.api.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ApiImageToggleView extends View {
    public static enum STATE{
        ON,
        OFF
    }

    private Context     context;
    private STATE       state;

    private Drawable    drawableOn;
    private Drawable    drawableOff;

    private int         drawableWidth;
    private int         drawableHeight;

    public ApiImageToggleView(Context context) {
        this(context, null);
    }

    public ApiImageToggleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ApiImageToggleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.context = context;
        _init(attrs, defStyle);
    }

    public void setState(STATE s) {
        if(state != s) {
            state = s;
            invalidate();
        }
    }

    public STATE getState() {
        return state;
    }

    public int getDrawableWidth() {
        return drawableWidth;
    }

    public int getDrawableHeight() {
        return drawableHeight;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(_getMeasureSize(widthMeasureSpec, drawableWidth), _getMeasureSize(heightMeasureSpec, drawableHeight));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed) {
            int width = right - left;
            int height = bottom - top;

            int l;
            int t;
            int r;
            int b;

            if( width > drawableWidth ) {
                l = ( width - drawableWidth ) / 2;
                r =  l + drawableWidth;
            } else {
                l = 0;
                r = width;
            }

            if( height > drawableHeight ) {
                t = ( height - drawableHeight ) / 2;
                b =  t + drawableHeight;
            } else {
                t = 0;
                b = height;
            }

            drawableOn.setBounds(l, t, r, b);
            drawableOff.setBounds(l, t, r, b);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(state == STATE.ON) {
            drawableOn.draw(canvas);
        } else {
            drawableOff.draw(canvas);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        int[] state = getDrawableState();

        if (drawableOn != null && drawableOn.isStateful()) {
            drawableOn.setState(state);
        }

        if (drawableOff != null && drawableOff.isStateful()) {
            drawableOff.setState(state);
        }

        invalidate();
    }

    private void _init(AttributeSet attrs, int defStyle) {
        state = STATE.ON;

        drawableOn = null;
        drawableOff = null;

        drawableWidth = 0;
        drawableHeight = 0;

        int drawableOnW  = 0, drawableOnH  = 0;
        int drawableOffW = 0, drawableOffH = 0;

        int[] ids = new int[attrs.getAttributeCount()];
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            ids[i] = attrs.getAttributeNameResource(i);
        }

        TypedArray a = context.obtainStyledAttributes(attrs, ids, defStyle, 0);

        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attrName = attrs.getAttributeName(i);
            if (attrName == null)
                continue;

            if (attrName.equals("btn_toggle_on")) {
                drawableOn = a.getDrawable(i);
                drawableOn.setCallback(this);
                drawableOn.setState(getDrawableState());
                drawableOn.setVisible(getVisibility() == VISIBLE, false);
                drawableOnW = drawableOn.getIntrinsicWidth();
                drawableOnH = drawableOn.getIntrinsicHeight();
            } else if (attrName.equals("btn_toggle_off")) {
                drawableOff = a.getDrawable(i);
                drawableOff.setCallback(this);
                drawableOff.setState(getDrawableState());
                drawableOff.setVisible(getVisibility() == VISIBLE, false);
                drawableOffW = drawableOff.getIntrinsicWidth();
                drawableOffH = drawableOff.getIntrinsicHeight();
            }
        }
        a.recycle();

        drawableWidth = drawableOnW > drawableOffW ?  drawableOnW : drawableOffW;
        drawableHeight = drawableOnH > drawableOffH ?  drawableOnH : drawableOffH;

        setClickable(true);
    }

    private int _getMeasureSize(int measureSpec, int cSize) {
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
