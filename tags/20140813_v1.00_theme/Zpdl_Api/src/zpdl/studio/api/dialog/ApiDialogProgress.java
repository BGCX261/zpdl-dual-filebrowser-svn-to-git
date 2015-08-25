/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zpdl.studio.api.dialog;

import zpdl.studio.api.theme.ApiTheme;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ApiDialogProgress extends ApiDialog {
    private ProgressBar mProgressView;
    private TextView    mMessageView;
    private TextView    mPercentView;
    private TextView    mCountView;

    private int         mMaxCnt;
    private int         mCount;
    private int         mProgress;

    public static ApiDialogProgress show(Activity a,
                                        int id,
                                        int titleResId,
                                        ApiDialogButtonParam[] btnParam) {
        return ApiDialogProgress.show(a.getFragmentManager(),
                                     id,
                                     a.getString(titleResId),
                                     btnParam);
    }

    public static ApiDialogProgress show(FragmentManager fm,
                                        int id,
                                        String title,
                                        ApiDialogButtonParam[] btnParam) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("khdialogprogress");

        if (prev != null) {
            ft.remove(prev);
        }

        Bundle args = new Bundle();
        args.putInt("id", id);
        if(title != null) {
            args.putString("title", title);
        }
        if(btnParam != null) {
            args.putParcelableArray("btnparam", btnParam);
        }

        // Create and show the dialog.
        ApiDialogProgress f = new ApiDialogProgress();
        f.setArguments(args);
        f.show(ft, "khdialogprogress");

        return f;
    }

    public int getMaxCount() {
        return mMaxCnt;
    }

    public void setMaxCount(int maxCnt) {
        if(mMaxCnt != maxCnt) {
            mMaxCnt = maxCnt;
            _setCount();
        }
    }

    public void setCount(int cnt) {
        if(mCount != cnt) {
            mCount = cnt;
            _setCount();
        }
    }

    public void setProgress(int progress) {
        if(mProgress != progress) {
            mProgress = progress;
            _setProgress();
        }
    }

    public void setMessage(String msg) {
        if(!mMessageView.getText().equals(msg)) {
            mMessageView.setText(msg);
            mMessageView.setVisibility(View.VISIBLE);
        }
    }

    public ApiDialogProgress() {
        super();

        mProgressView = null;
        mMessageView = null;
        mPercentView = null;
        mCountView = null;

        mMaxCnt = 0;
        mCount = 0;
        mProgress = 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence("message", mMessageView.getText());
        outState.putInt("maxcount", mMaxCnt);
        outState.putInt("count", mCount);
        outState.putInt("progress", mProgress);
    }

    @Override
    protected View _onCreateView(Context context, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);

        float dip = ApiTheme.Dip();

        CharSequence message = null;
        if (savedInstanceState != null) {
            message   = savedInstanceState.getCharSequence("message", null);
            mMaxCnt   = savedInstanceState.getInt("maxcount", 0);
            mCount    = savedInstanceState.getInt("count", 0);
            mProgress = savedInstanceState.getInt("progress", 0);
        }

        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.setPadding((int) (10 * dip), (int) (15 * dip), (int) (10 * dip), (int) (5 * dip));

        mMessageView = new TextView(context);
        mMessageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mMessageView.setPadding((int) (10 * dip), (int) (5 * dip), (int) (10 * dip), (int) (10 * dip));
        mMessageView.setGravity(Gravity.CENTER);
        mMessageView.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
        mMessageView.setTextColor(ApiTheme.Color(context, ApiTheme.ForegroundColorId()));
        mMessageView.setTextSize(ApiTheme.TextSize(16f));

        if(message != null) {
            mMessageView.setText(message);
            mMessageView.setVisibility(View.VISIBLE);
        } else
            mMessageView.setVisibility(View.GONE);
        ll.addView(mMessageView);

        mProgressView = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        mProgressView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mProgressView.setProgressDrawable(context.getResources().getDrawable(ApiTheme.DialogProgressHorizontalId()));
        mProgressView.setIndeterminateDrawable(context.getResources().getDrawable(ApiTheme.DialogProgressIndeterminateHorizontalId()));
        ll.addView(mProgressView);

        LinearLayout cll = new LinearLayout(context);
        cll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cll.setPadding(0, (int) (1 * dip), 0, (int) (10 * dip));
        cll.setOrientation(LinearLayout.HORIZONTAL);

        mPercentView = new TextView(context);
        mPercentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        mPercentView.setPadding(0, 0, (int) (5 * dip), 0);
        mPercentView.setGravity(Gravity.LEFT);
        mPercentView.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
        mPercentView.setTextColor(ApiTheme.Color(context, ApiTheme.ForegroundColorId()));
        mPercentView.setTextSize(ApiTheme.TextSize(14f));
        cll.addView(mPercentView);

        mCountView = new TextView(context);
        mCountView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        mCountView.setPadding((int) (5 * dip), 0, 0, 0);
        mCountView.setGravity(Gravity.RIGHT);
        mCountView.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
        mCountView.setTextColor(ApiTheme.Color(context, ApiTheme.ForegroundColorId()));
        mCountView.setTextSize(ApiTheme.TextSize(14f));
        cll.addView(mCountView);

        _setProgress();
        _setCount();
        ll.addView(cll);

        return ll;
    }

    private void _setProgress() {
        if(mProgressView != null) {
            mProgressView.setProgress(mProgress);
        }
        if(mPercentView != null) {
            mPercentView.setText(String.format("%d %%", mProgress));
        }
    }

    private void _setCount() {
        if(mCountView != null) {
            mCountView.setText(String.format("%d / %d", mCount, mMaxCnt));
        }
    }


}
