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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ApiDialogContextMenu extends ApiDialog {
    public static ApiDialogContextMenu show(FragmentManager fm,
                                               int id,
                                               int[] itemId,
                                               String[] itemSubject) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("ApiDialogContextMenu");

        if (prev != null) {
            ft.remove(prev);
        }

        Bundle args = new Bundle();
        args.putInt("id", id);
        if(itemId != null) {
            args.putIntArray("itemId", itemId);
        }
        if(itemSubject != null) {
            args.putStringArray("itemSubject", itemSubject);
        }

        // Create and show the dialog.
        ApiDialogContextMenu f = new ApiDialogContextMenu();
        f.setArguments(args);
        f.show(ft, "ApiDialogContextMenu");

        return f;
    }

    public ApiDialogContextMenu() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected View _onCreateView(Context context, Bundle savedInstanceState) {
        int[] itemId = getArguments().getIntArray("itemId");
        String[] itemSubject = getArguments().getStringArray("itemSubject");

        ScrollView sv = new ScrollView(context);
        sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.VERTICAL);

        for(int i = 0; i < itemId.length; i++) {
            Button btn = new Button(context);
            btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            btn.setBackgroundResource(ApiTheme.SelectorListDrawableId());
            btn.setTextColor(ApiTheme.Color(context, ApiTheme.DialogButtonTextColorId()));
            btn.setTypeface(Typeface.SANS_SERIF);
            btn.setText(itemSubject[i]);
            btn.setTextSize(ApiTheme.TextSize(16f));
            btn.setTag(Integer.valueOf(itemId[i]));
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null) mListener.onButtonClick(mDialog, ((Integer) v.getTag()).intValue());
                    mDialog.dismiss();
                }
            });
            ll.addView(btn);
        }
        sv.addView(ll);

        return sv;
    }
}
