package zpdl.studio.api.dialog;

import zpdl.studio.api.theme.ApiTheme;
import zpdl.studio.api.util.ApiLog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ApiDialogGroupView extends ViewGroup {
    private Context mContext;

    private View mTitle;
    private View mBody;
    private View mButton;
    private onDialogButtonClickListener mDialogButtonClickListener;

    public interface onDialogButtonClickListener {
        public void onDialogButtonClick(int id);
    }

    public ApiDialogGroupView(Context context) {
        this(context, null);
    }

    public ApiDialogGroupView(Context context, AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public ApiDialogGroupView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;

        mTitle = null;
        mBody = null;
        mButton = null;
        mDialogButtonClickListener = null;
        this.setBackgroundResource(ApiTheme.BackgoundId());
    }

    public void setTitle(String title) {
        float dip =  ApiTheme.Dip();

        LinearLayout ll = new LinearLayout(mContext);
        ll.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setPaddingRelative((int) (16 * dip), 0, (int) (16 * dip), 0);
        tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        tv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
        tv.setTextColor(ApiTheme.Color(mContext, ApiTheme.DialogTitleColorId()));
        tv.setMinHeight((int) (58 * dip));
        tv.setTextSize(ApiTheme.TextSize(28f));
        tv.setText(title);
        ll.addView(tv);

        View divider = new View(mContext);
        divider.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (2 * dip)));
        divider.setBackgroundResource(ApiTheme.DialogDividerDrawableId());
        ll.addView(divider);

        mTitle = ll;
        addView(mTitle);
    }

    public void setBodyView(View body) {
        if(body != null) {
            mBody = body;
            addView(mBody);
        }
    }

    public void setButtonView(ApiDialogButtonParam[] param, onDialogButtonClickListener listener) {
        if(param == null) {
            return;
        }

        mDialogButtonClickListener = listener;

        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mContext.getResources().getDisplayMetrics());

        LinearLayout ll = new LinearLayout(mContext);
        ll.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding((int)(1 * dip), (int)(1 * dip), (int)(1 * dip), (int)(1 * dip));

        for(int i = 0; i < param.length; i++) {
            Button btn = new Button(mContext);
            btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            btn.setBackgroundResource(ApiTheme.DialogButtonSelectorId());
            btn.setTextColor(ApiTheme.Color(mContext, ApiTheme.DialogButtonTextColorId()));
            btn.setTypeface(Typeface.SANS_SERIF);
            btn.setText(param[i].text);
//            btn.setEnabled(false);
            btn.setTag(Integer.valueOf(param[i].id));
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mDialogButtonClickListener != null) {
                        mDialogButtonClickListener.onDialogButtonClick(((Integer) v.getTag()).intValue());
                    }
                }
            });
            ll.addView(btn);
        }
        mButton = ll;
        addView(mButton);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cwidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                                    MeasureSpec.getSize(widthMeasureSpec),
                                    MeasureSpec.EXACTLY);
//        int cwidthMeasureSpec = widthMeasureSpec;
        if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            ApiLog.i("onMeasure mode MeasureSpec.UNSPECIFIED = %x", MeasureSpec.UNSPECIFIED);
        } else if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            ApiLog.i("onMeasure mode MeasureSpec.EXACTLY = %x ", MeasureSpec.EXACTLY);
        } else if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            ApiLog.i("onMeasure mode MeasureSpec.AT_MOST = %x", MeasureSpec.AT_MOST);
        } else {
            ApiLog.i("onMeasure mode Unknown = %x", MeasureSpec.getMode(widthMeasureSpec));
        }

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int childheight = 0;

        if(mTitle != null) {
            measureChild(mTitle, cwidthMeasureSpec, heightMeasureSpec);
            heightSize -= mTitle.getMeasuredHeight();
            childheight += mTitle.getMeasuredHeight();
        }

        if(mButton != null) {
            measureChild(mButton, cwidthMeasureSpec, heightMeasureSpec);
            heightSize -= mButton.getMeasuredHeight();
            childheight += mButton.getMeasuredHeight();
        }

        if(mBody != null) {
            int bodyHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.getMode(heightMeasureSpec));
            measureChild(mBody, cwidthMeasureSpec, bodyHeightMeasureSpec);
            childheight += mBody.getMeasuredHeight();
        }

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(childheight, MeasureSpec.getMode(heightMeasureSpec)));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        ApiLog.i("onLayout changed = %b l = %d t = %d r = %d b = %d",changed, l, t, r, b);
        int heightPos = 0;
        if(mTitle != null) {
            mTitle.layout(0, heightPos, mTitle.getMeasuredWidth(), heightPos + mTitle.getMeasuredHeight());
            heightPos += mTitle.getMeasuredHeight();
        }

        if(mBody != null) {
            mBody.layout(0, heightPos, mBody.getMeasuredWidth(), heightPos + mBody.getMeasuredHeight());
            heightPos += mBody.getMeasuredHeight();
        }
        if(mButton != null) {
            mButton.layout(0, heightPos, mButton.getMeasuredWidth(), heightPos + mButton.getMeasuredHeight());
        }
    }

}
