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
import android.widget.TextView;

public class ApiDialogSimple extends ApiDialog {
    private String              mBody;

    public static ApiDialogSimple show(Activity a,
                                      int id,
                                      int titleResId,
                                      int bodyResId,
                                      ApiDialogButtonParam[] btnParam) {
        return ApiDialogSimple.show(a.getFragmentManager(),
                                   id,
                                   a.getString(titleResId),
                                   a.getString(bodyResId),
                                   btnParam);
    }

    public static ApiDialogSimple show(FragmentManager fm,
                                      int id,
                                      String title,
                                      String body,
                                      ApiDialogButtonParam[] btnParam) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("khdialogsimple");

        if (prev != null) {
            ft.remove(prev);
        }

        Bundle args = new Bundle();
        args.putInt("id", id);
        if(title != null) {
            args.putString("title", title);
        }
        if(body != null) {
            args.putString("body", body);
        }
        if(btnParam != null) {
            args.putParcelableArray("btnparam", btnParam);
        }

        // Create and show the dialog.
        ApiDialogSimple f = new ApiDialogSimple();
        f.setArguments(args);
        f.show(ft, "khdialogsimple");

        return f;
    }

    public ApiDialogSimple() {
        super();

        mBody     = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBody     = getArguments().getString("body", null);
    }

    @Override
    protected View _onCreateView(Context context, Bundle savedInstanceState) {
        float dip = ApiTheme.Dip();

        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.setPadding((int) (10 * dip), (int) (15 * dip), (int) (10 * dip), (int) (14 * dip));

        if(mBody != null) {
            TextView tv = new TextView(context);
            tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTypeface(Typeface.SANS_SERIF, 0);
            tv.setTextColor(context.getResources().getColor(ApiTheme.ForegroundColorId()));
            tv.setTextSize(ApiTheme.TextSize(20f));
            tv.setText(mBody);
            ll.addView(tv);
        }

        return ll;
    }
}
