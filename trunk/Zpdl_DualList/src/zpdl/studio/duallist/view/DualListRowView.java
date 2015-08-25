package zpdl.studio.duallist.view;

import zpdl.studio.duallist.DualListTheme;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DualListRowView extends ViewGroup {
    private static int PaddingLeft      = 4;
    private static int PaddingTop       = 4;
    private static int PaddingRight     = 2;
    private static int PaddingBottom    = 2;
    private static int PaddingIcon      = 4;
    private static int PaddingClickable = 2;

    private static int MainIconWidth    = 48;
    private static int MainIconHeight   = 48;
    private static int ClickableWidth   = 2;

    private ImageView mMainIcon;
    private ImageView mSubIcon;
    private ImageView mClickable;
    private TextView  mName;
    private LinearLayout mInfoLL;
    private TextView  minfoSize;
    private TextView  mInfoExt;

    private String    mPath;
    private float     mDip;

    public DualListRowView(Context context) {
        this(context, null);
    }

    public DualListRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DualListRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mDip = DualListTheme.Dip();

        _createLayout(context);
    }

    public static int getIconWidth(Context context, float scale) {
        float dipScale = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics()) * scale;

        int paddingleft = (int)(PaddingLeft * dipScale);
        int iconWidth = (int)(MainIconWidth * dipScale) + (int)(MainIconWidth * dipScale) / 8;
        int iconpadding = (int)(PaddingIcon * dipScale) / 2;

        return paddingleft + iconWidth + iconpadding;
    }

    public static int getMainIconWidth(Context context, float scale) {
        float dipScale = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics()) * scale;

        return (int)(MainIconWidth * dipScale);
    }

    public static int getMainIconHeight(Context context, float scale) {
        float dipScale = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics()) * scale;

        return (int)(MainIconHeight * dipScale);
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getPath() {
        return mPath;
    }

    public void setName(String text) {
        mName.setText(text);
    }

    public void setInfoSize(long size) {
        mInfoLL.setVisibility(View.VISIBLE);
        String text = null;
        if(size > 1024*1024*1024) {
            float tSize = size / (float) (1024*1024*1024);
            text = String.format("%.1f Gb", tSize);
        } else if(size > 1024*1024) {
            float tSize = size / (float) (1024*1024);
            text = String.format("%.1f Mb", tSize);
        } else if(size > 1024) {
            float tSize = size / (float) 1024;
            text = String.format("%.1f Kb", tSize);
        } else {
            text = String.format("%d B", size);
        }
        minfoSize.setText(text);
    }

    public void setInfoExt(String text) {
        mInfoLL.setVisibility(View.VISIBLE);
        mInfoExt.setText(text);
    }

    public void setInfoGone() {
        mInfoLL.setVisibility(View.GONE);
    }

    public void setMainDrawable(Drawable d) {
        mMainIcon.setImageDrawable(d);
    }

    public void setMainIcon(Bitmap bm) {
        mMainIcon.setImageBitmap(bm);
    }

    public void setMainIcon(int resId) {
        mMainIcon.setImageResource(resId);
    }

    public void setSubDrawable(Drawable d) {
        mSubIcon.setVisibility(View.VISIBLE);
        mSubIcon.setImageDrawable(d);
    }

    public void setSubIcon(int resId) {
        mSubIcon.setVisibility(View.VISIBLE);
        mSubIcon.setImageResource(resId);
    }

    public void setSubIconGone() {
        mSubIcon.setVisibility(View.GONE);
    }

    public void setClickable(boolean enable) {
        mClickable.setVisibility(View.VISIBLE);
        if(enable) {
            mClickable.setImageResource(DualListTheme.ListClickEnableId());
        } else {
            mClickable.setImageResource(DualListTheme.ListClickDisableId());
        }
    }

    public void setClickableGone() {
        mClickable.setVisibility(View.GONE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();

        measureChild(mMainIcon, widthMeasureSpec, heightMeasureSpec);
        measureChild(mSubIcon, widthMeasureSpec, heightMeasureSpec);

        int iconWidth = mMainIcon.getMeasuredWidth() + mSubIcon.getMeasuredWidth() / 4;
        int iconHeight = mMainIcon.getMeasuredHeight() + mSubIcon.getMeasuredHeight() / 4;

        int textSize = 0;
        if(mClickable.getVisibility() == View.VISIBLE) {
            textSize = widthSize - iconWidth - (int)((PaddingIcon + PaddingClickable + ClickableWidth) * mDip);
        } else {
            textSize = widthSize - iconWidth - (int)(PaddingIcon * mDip);
        }
        int textWidthMeasureSpec = MeasureSpec.makeMeasureSpec(textSize, MeasureSpec.getMode(widthMeasureSpec));
        measureChild(mName, textWidthMeasureSpec, heightMeasureSpec);
        measureChild(mInfoLL, textWidthMeasureSpec, heightMeasureSpec);

        int heightSize = getPaddingTop() + getPaddingBottom();
        if(mInfoLL.getVisibility() == View.VISIBLE) {
            int textHeight = mName.getMeasuredHeight() + mInfoLL.getMeasuredHeight();
            heightSize += iconHeight > textHeight ? iconHeight : textHeight;
        } else {
            int textHeight = mName.getMeasuredHeight();
            heightSize += iconHeight > textHeight ? iconHeight : textHeight;
        }

        measureChild(mClickable, widthMeasureSpec, heightMeasureSpec);

        int cHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.getMode(heightMeasureSpec));
        setMeasuredDimension(widthMeasureSpec, cHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;

        int width = r - l - getPaddingLeft() - getPaddingRight();
        int height = b - t - getPaddingTop() - getPaddingBottom();

        int iconWidth = mMainIcon.getMeasuredWidth() + mMainIcon.getMeasuredWidth() / 8;
        int iconHeight = mMainIcon.getMeasuredHeight() + mMainIcon.getMeasuredHeight() / 8;

        left = getPaddingLeft();
        top = getPaddingTop() + (height - iconHeight) / 2;
        right = left + mMainIcon.getMeasuredWidth();
        bottom = top + mMainIcon.getMeasuredHeight();
        mMainIcon.layout(left, top, right, bottom);

        if(mSubIcon.getVisibility() == View.VISIBLE) {
            left = getPaddingLeft() + iconWidth - mSubIcon.getMeasuredWidth();
            top = getPaddingTop() + (height - iconHeight) / 2 + iconHeight - mSubIcon.getMeasuredHeight();
            right = left + mSubIcon.getMeasuredWidth();
            bottom = top + mSubIcon.getMeasuredHeight();
            mSubIcon.layout(left, top, right, bottom);
        }

        if(mInfoLL.getVisibility() == View.VISIBLE) {
            left = getPaddingLeft() + iconWidth + (int)(PaddingIcon * mDip);
            top = getPaddingTop() + (height - mInfoLL.getMeasuredHeight() - mName.getMeasuredHeight()) / 2;
            right = left + mName.getMeasuredWidth() + 10;
            bottom = top + mName.getMeasuredHeight();
            mName.layout(left, top, right, bottom);

            top = getPaddingTop() + height - mInfoLL.getMeasuredHeight();
            right = left + mInfoLL.getMeasuredWidth();
            bottom = top + mInfoLL.getMeasuredHeight();
            mInfoLL.layout(left, top, right, bottom);
        } else {
            left = getPaddingLeft() + iconWidth + (int)(PaddingIcon * mDip);
            top = getPaddingTop() + (height - mName.getMeasuredHeight()) / 2;
            right = left + mName.getMeasuredWidth();
            bottom = top + mName.getMeasuredHeight();
            mName.layout(left, top, right, bottom);
        }

        left = getPaddingLeft() + width - mClickable.getMeasuredWidth();
        top = 0;
        right = left + mClickable.getMeasuredWidth();
        bottom = b - t;
        mClickable.layout(left, top, right, bottom);
    }

    private void _createLayout(Context context) {
        this.setPadding((int)(PaddingLeft * mDip), (int)(PaddingTop * mDip), (int)(PaddingRight * mDip), (int)(PaddingBottom * mDip));

        mMainIcon = new ImageView(context);
        mMainIcon.setLayoutParams(new LayoutParams((int)(MainIconWidth * mDip), (int)(MainIconHeight * mDip)));
        addView(mMainIcon);

        mSubIcon = new ImageView(context);
        mSubIcon.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mSubIcon);

        mName = new TextView(context);
        mName.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mName.setTextAppearance(context, DualListTheme.TextApperranceRowNameId());
        mName.setTextSize(DualListTheme.TextSize(16f));
        addView(mName);

        mInfoLL = new LinearLayout(context);
        mInfoLL.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mInfoLL.setOrientation(LinearLayout.HORIZONTAL);

        minfoSize = new TextView(context);
        minfoSize.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        minfoSize.setTextAppearance(context, DualListTheme.TextApperranceRowInfoId());
        minfoSize.setTextSize(DualListTheme.TextSize(12f));
        mInfoLL.addView(minfoSize);

        mInfoExt = new TextView(context);
        mInfoExt.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mInfoExt.setGravity(Gravity.RIGHT);
        mInfoExt.setTextAppearance(context, DualListTheme.TextApperranceRowInfoId());
        mInfoExt.setTextSize(DualListTheme.TextSize(12f));
        mInfoLL.addView(mInfoExt);
        addView(mInfoLL);

        mClickable = new ImageView(context);
        mClickable.setLayoutParams(new LayoutParams((int)(ClickableWidth * mDip), ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mClickable);
    }
}
