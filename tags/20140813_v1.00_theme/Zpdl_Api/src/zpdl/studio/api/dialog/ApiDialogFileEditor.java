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

import zpdl.studio.api.R;
import zpdl.studio.api.theme.ApiTheme;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

public class ApiDialogFileEditor extends ApiDialog {
    private String                  mEdit;
    private String                  mHint;

    private EditText                mEditTextView;

    public static ApiDialogFileEditor show(Activity a,
                                          int id,
                                          int titleResId,
                                          String edit,
                                          int hintResId,
                                          ApiDialogButtonParam[] btnParam) {
        return ApiDialogFileEditor.show(a.getFragmentManager(),
                                       id,
                                       a.getString(titleResId),
                                       edit,
                                       a.getString(hintResId),
                                       btnParam);
    }

    public static ApiDialogFileEditor show(FragmentManager fm,
                                          int id,
                                          String title,
                                          String edit,
                                          String hint,
                                          ApiDialogButtonParam[] btnParam) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("khdialogfileeditor");

        if (prev != null) {
            ft.remove(prev);
        }

        Bundle args = new Bundle();
        args.putInt("id", id);
        if(title != null) {
            args.putString("title", title);
        }
        if(hint != null) {
            args.putString("hint", hint);
        }
        if(btnParam != null) {
            args.putParcelableArray("btnparam", btnParam);
        }

        // Create and show the dialog.
        ApiDialogFileEditor f = new ApiDialogFileEditor();
        f.setEditText(edit);
        f.setArguments(args);
        f.show(ft, "khdialogfileeditor");

        return f;
    }

    public void setEditText(String text) {
        mEdit = text;
        if(mEditTextView != null) {
            mEditTextView.setText(mEdit);
        }
    }

    public String getEditText() {
        return mEditTextView.getText().toString();
    }

    public ApiDialogFileEditor() {
        super();

        mEdit     = null;
        mHint     = null;

        mEditTextView = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHint = getArguments().getString("hint", null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("edit", mEditTextView.getText().toString());
        outState.putInt("editselectionstart", mEditTextView.getSelectionStart());
        outState.putInt("editselectionend", mEditTextView.getSelectionEnd());
    }

    @Override
    protected View _onCreateView(Context context, Bundle savedInstanceState) {
        float dip = ApiTheme.Dip();

//        LinearLayout ll = new LinearLayout(context);
//        ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        ll.setOrientation(LinearLayout.VERTICAL);
//        ll.setGravity(Gravity.CENTER_HORIZONTAL);
//        ll.setPadding((int) (10 * dip), (int) (15 * dip), (int) (10 * dip), (int) (4 * dip));
//
//        mEditTextView = new EditText(context);
//        mEditTextView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        mEditTextView.setPadding((int) (10 * dip), (int) (10 * dip), (int) (10 * dip), (int) (5 * dip));
//        mEditTextView.setBackgroundResource(R.drawable.api_bg_edittext_textfield);
//        mEditTextView.setTypeface(Typeface.SANS_SERIF, 0);
//        mEditTextView.setTextColor(ApiTheme.getColor(context, ApiTheme.getBaseTextColorId()));
//
//
//        mEditTextView.setTextSize(ApiTheme.getTextSize(20f));
//        mEditTextView.setPrivateImeOptions("inputType=filename;defaultInputmode=english");
//        mEditTextView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
//        if(mHint != null) mEditTextView.setHint(mHint);
//        mEditTextView.requestFocus();
//
//        ll.addView(mEditTextView);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        FrameLayout fl = (FrameLayout) inflater.inflate(ApiTheme.DialogFileEditorLayoutId(), null);
        fl.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        fl.setPadding((int) (10 * dip), (int) (15 * dip), (int) (10 * dip), (int) (15 * dip));

        mEditTextView = (EditText) fl.findViewById(R.id.dialog_file_editor);
        mEditTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mEditTextView.setPadding((int) (10 * dip), (int) (20 * dip), (int) (10 * dip), (int) (10 * dip));
        mEditTextView.setTextSize(ApiTheme.TextSize(20f));
        if(mHint != null) mEditTextView.setHint(mHint);
        mEditTextView.requestFocus();

        if (savedInstanceState != null) {
            mEdit = savedInstanceState.getString("edit", null);
            int selectionStart = savedInstanceState.getInt("editselectionstart", -1);
            int selectionEnd = savedInstanceState.getInt("editselectionend", -1);
            if(mEdit != null) {
                mEditTextView.setText(mEdit);
                mEditTextView.setSelection(selectionStart, selectionEnd);
            }
        } else {
            if(mEdit != null) {
                int separatorIndex = mEdit.lastIndexOf('.');
                separatorIndex = (separatorIndex < 0) ? mEdit.length() : separatorIndex;
                mEditTextView.setText(mEdit);
                mEditTextView.setSelection(0, separatorIndex);
            }
        }

        new Handler().postDelayed(new Runnable(){
            public void run(){
                InputMethodManager mgr = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.showSoftInput(mEditTextView, InputMethodManager.SHOW_IMPLICIT);
            }
            }, 100 );

        return fl;
    }
}
