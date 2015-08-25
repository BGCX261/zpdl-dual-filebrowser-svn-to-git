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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

public abstract class ApiDialog extends DialogFragment {

    public interface ApiDialogListener {
        public void onDialogLoaded(ApiDialog dialog);
        public void onDialogUnLoaded(ApiDialog dialog);
        public void onButtonClick(ApiDialog dialog, int id);
    }

    protected ApiDialogListener     mListener;

    protected int                   mId;
    protected String                mTitle;

    protected ApiDialog              mDialog;
    protected ApiDialogButtonParam[] mBtnParam;

    public ApiDialog() {
        super();

        mDialog = this;
        mId = 0;
        mTitle = null;
        mBtnParam = null;

        mListener = null;
    }

    public int getDialogId() {
        return mId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mId       = getArguments().getInt("id", 0);
        mTitle    = getArguments().getString("title", null);
        mBtnParam = (ApiDialogButtonParam[]) getArguments().getParcelableArray("btnparam");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ApiDialogListener) activity;
            mListener.onDialogLoaded(mDialog);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement KhDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ApiDialogGroupView dialogGroupView = new ApiDialogGroupView(getActivity());
        dialogGroupView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if(mTitle != null)
            dialogGroupView.setTitle(mTitle);
        dialogGroupView.setBodyView(_onCreateView(getActivity(), savedInstanceState));
        dialogGroupView.setButtonView(mBtnParam, new ApiDialogGroupView.onDialogButtonClickListener() {
            @Override
            public void onDialogButtonClick(int id) {
                if(mListener != null) mListener.onButtonClick(mDialog, id);
            }
        });

        return dialogGroupView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDialogUnLoaded(mDialog);
    }

    protected abstract View _onCreateView(Context context, Bundle savedInstanceState);
}
