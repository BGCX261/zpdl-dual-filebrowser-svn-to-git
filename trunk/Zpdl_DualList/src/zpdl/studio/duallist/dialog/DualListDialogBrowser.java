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

package zpdl.studio.duallist.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import zpdl.studio.api.dialog.ApiDialog;
import zpdl.studio.api.dialog.ApiDialogButtonParam;
import zpdl.studio.api.theme.ApiTheme;
import zpdl.studio.api.util.ApiExternalStorage;
import zpdl.studio.duallist.DualListTheme;
import zpdl.studio.duallist.R;
import zpdl.studio.duallist.view.DualListItem;
import zpdl.studio.duallist.view.DualListView;
import zpdl.studio.duallist.view.DualListView.onFileBrowserListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DualListDialogBrowser extends ApiDialog {
    private String              mPath;
    private TextView            mPathView;
    private DualListView        mFolder;

    private String              mSdCard;
    private String[]            mExtsdCard;
    private String              mRoot;

    public static DualListDialogBrowser show(Activity a,
                                                int id,
                                                int titleResId,
                                                String path,
                                                ApiDialogButtonParam[] btnParam) {
        return DualListDialogBrowser.show(a.getFragmentManager(),
                                             id,
                                             a.getString(titleResId),
                                             path,
                                             btnParam);
    }

    public static DualListDialogBrowser show(FragmentManager fm,
                                                int id,
                                                String title,
                                                String path,
                                                ApiDialogButtonParam[] btnParam) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("filebrowserdialog");

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
        DualListDialogBrowser f = new DualListDialogBrowser();
        f.setPath(path);
        f.setArguments(args);
        f.show(ft, "filebrowserdialog");

        return f;
    }



    public void setPath(String path) {
        mPath = path;
    }

    public String getPath() {
        return mPath;
    }

    public DualListDialogBrowser() {
        super();

        mPath  = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("path", mPath);
    }

    @Override
    protected View _onCreateView(Context context, Bundle savedInstanceState) {
        float dip = ApiTheme.Dip();

        if (savedInstanceState != null) {
            mPath = savedInstanceState.getString("path", null);
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.dialog_folderbrowser, null);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mPathView = (TextView) ll.findViewById(R.id.dialog_folderbrowser_path);
        mPathView.setPadding((int)(6 * dip), (int)(8 * dip), (int)(6 * dip), (int)(8 * dip));
        mPathView.setTextAppearance(context, DualListTheme.TextApperrancePathId());
        mPathView.setTextSize(ApiTheme.TextSize(16f));

        mFolder = (DualListView) ll.findViewById(R.id.dialog_folderbrowser_browser);
        mFolder.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mFolder.setonFileBrowserListener(new onFileBrowserListener() {
            @Override
            public void onClick(DualListItem item) {
                mPath = item.getPath();
                _init();
            }

            @Override
            public boolean onLongClick(DualListItem item) {
                return false;
            }
        });

        _initRoot();
        _init();

        return ll;
    }

    private void _init() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if(!mPath.startsWith(mSdCard)) {
                mPath = mSdCard;
            }
        }

        String  parentPath = mPath.lastIndexOf(File.separator) > 0 ?
                             mPath.substring(0, mPath.lastIndexOf(File.separator)) : null;
        mPathView.setText(parentPath);

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if(mPath.equals(mSdCard)) {
                parentPath = null;
            }
        }

        if(!(new File(mPath)).exists() || mPath.equals(mRoot)) {
            _initRoot();

            String[] sdcard = null;
            if(mExtsdCard != null) {
                sdcard = new String[1 + mExtsdCard.length];
                sdcard[0] = mSdCard;
                for(int i = 1; i < mExtsdCard.length+1; i++) {
                    sdcard[i] = mExtsdCard[i-1];
                }
            } else {
                sdcard = new String[1];
                sdcard[0] = mSdCard;
            }
            mFolder.initSDCard(sdcard);
        } else {
            ArrayList<String> folderList = new ArrayList<String>();

            File[] files = new File(mPath).listFiles();
            if(files != null) {
                for(File file: files) {
                    if(file.isHidden()) {
                        continue;
                    } else if(file.isDirectory()) {
                        folderList.add(file.getPath());
                    }
                }
            }

            if(folderList.size() > 0) {
                Collections.sort(folderList, String.CASE_INSENSITIVE_ORDER);
                String[] listParam = new String[folderList.size()];
                for(int i = 0; i < folderList.size(); i++) {
                    listParam[i] = folderList.get(i);
                }
                mFolder.initFileList(listParam, parentPath);
            } else {
                mFolder.initFileList(null, parentPath);
            }
        }

    }

    private void _initRoot() {
        Map<String, File> externalLocations = ApiExternalStorage.getAllStorageLocations();
        mSdCard = externalLocations.get(ApiExternalStorage.SD_CARD).getPath();
        mExtsdCard = null;
        if(externalLocations.size() > 1) {
            mExtsdCard = new String[externalLocations.size() - 1];
            for(int i = 1; i < externalLocations.size(); i++) {
                mExtsdCard[i-1] = externalLocations.get(ApiExternalStorage.EXTERNAL_SD_CARD  + "_" + i).getPath();
            }
        }
        mRoot = mSdCard.substring(0, mSdCard.lastIndexOf(File.separator));
    }
}
