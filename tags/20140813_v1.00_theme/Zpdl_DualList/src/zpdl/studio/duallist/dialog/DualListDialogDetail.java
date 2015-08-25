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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import zpdl.studio.api.dialog.ApiDialog;
import zpdl.studio.api.dialog.ApiDialogButtonParam;
import zpdl.studio.api.drawable.ApiBitmapFactory;
import zpdl.studio.api.drawable.ApiThumbnailParam;
import zpdl.studio.api.theme.ApiTheme;
import zpdl.studio.api.util.ApiLog;
import zpdl.studio.duallist.DualListTheme;
import zpdl.studio.duallist.R;
import zpdl.studio.duallist.view.DualListItem;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DualListDialogDetail extends ApiDialog {
    private String              mPath;

    public static DualListDialogDetail show(Activity a,
                                               int id,
                                               int titleResId,
                                               String path,
                                               ApiDialogButtonParam[] btnParam) {
        return DualListDialogDetail.show(a.getFragmentManager(),
                                            id,
                                            a.getString(titleResId),
                                            path,
                                            btnParam);
    }

    public static DualListDialogDetail show(FragmentManager fm,
                                               int id,
                                               String title,
                                               String path,
                                               ApiDialogButtonParam[] btnParam) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("FileBrowserDialogDetail");

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
        DualListDialogDetail f = new DualListDialogDetail();
        f.setPath(path);
        f.setArguments(args);
        f.show(ft, "FileBrowserDialogDetail");

        return f;
    }

    public DualListDialogDetail() {
        super();

        mPath  = null;
    }

    public void setPath(String path) {
        mPath = path;
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
        if(savedInstanceState != null) {
            mPath = savedInstanceState.getString("path");
        }

        View v = null;
        DualListItem item = new DualListItem(mPath);
        ApiLog.i("item.getType() = %d", item.getType(context.getPackageManager()));
        switch(item.getType()) {
        case DualListItem.MP3 :
            v = _createMP3View(context, mPath);
            break;
        case DualListItem.IMAGE :
            v = _createImagelView(context, mPath);
            break;
        case DualListItem.VIDEO :
            v = _createVideoView(context, mPath);
            break;

        default :
            v = _createNormalView(context, mPath);
        }

        return v;
    }

    private View _createNormalView(Context context, String path) {
        float dip =  ApiTheme.Dip();

        File file = new File(path);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ScrollView sv = (ScrollView) inflater.inflate(R.layout.dialog_detail_normal, null);
        sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LinearLayout ll = (LinearLayout) sv.findViewById(R.id.dialog_detail_layout);
        ll.setPadding((int)(10 * dip), (int)(15 * dip), (int)(10 * dip), (int)(5 * dip));

        TextView tv = null;
        tv = (TextView) ll.findViewById(R.id.dialog_detail_filename);
        tv.setPadding((int)(10 * dip), 0, (int)(10 * dip), 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailNameId());
        tv.setTextSize(DualListTheme.TextSize(18f));
        tv.setText(file.getName());

        tv = (TextView) ll.findViewById(R.id.dialog_detail_size_subject);
        tv.setPadding(0, (int)(20 * dip), (int)(10 * dip), 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_size_content);
        tv.setPadding(0, (int)(20 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(_getTextForSize(file.length()));

        tv = (TextView) ll.findViewById(R.id.dialog_detail_folder_subject);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_folder_content);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(file.getParent());

        tv = (TextView) ll.findViewById(R.id.dialog_detail_last_modified_time_subject);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_last_modified_time_content);
        tv.setPadding(0, 0, 0, (int)(10 * dip));
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(_getTextForDate(file.lastModified()));

        return sv;
    }

    private View _createImagelView(Context context, String path) {
        float dip =  ApiTheme.Dip();

        ApiThumbnailParam thumbnailParam = ApiBitmapFactory.createThumbnailOnHeight(path, (int)(64 * dip));

        File file = new File(path);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ScrollView sv = (ScrollView) inflater.inflate(R.layout.dialog_detail_image, null);
        sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LinearLayout ll = (LinearLayout) sv.findViewById(R.id.dialog_detail_layout);
        ll.setPadding((int)(10 * dip), (int)(15 * dip), (int)(10 * dip), (int)(5 * dip));

        TextView tv = null;
        tv = (TextView) ll.findViewById(R.id.dialog_detail_filename);
        tv.setPadding((int)(10 * dip), 0, (int)(10 * dip), 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailNameId());
        tv.setTextSize(DualListTheme.TextSize(18f));
        tv.setText(file.getName());

        if(thumbnailParam.thumbnail != null) {
            tv.setCompoundDrawablePadding((int)(10 * dip));
            Drawable leftDrawable = new BitmapDrawable(context.getResources(), thumbnailParam.thumbnail);
            leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(), leftDrawable.getIntrinsicHeight());
            if(thumbnailParam.thumbnail.getWidth() / dip > 100) {
                tv.setCompoundDrawables(null, leftDrawable, null, null);
            } else {
                tv.setCompoundDrawables(leftDrawable, null, null, null);
            }
        }

        tv = (TextView) ll.findViewById(R.id.dialog_detail_resolution_subject);
        tv.setPadding(0, (int)(20 * dip), (int)(10 * dip), 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_resolution_content);
        tv.setPadding(0, (int)(20 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(String.format("%d x %d", thumbnailParam.width, thumbnailParam.height));

        tv = (TextView) ll.findViewById(R.id.dialog_detail_size_subject);
        tv.setPadding(0, (int)(7 * dip), (int)(10 * dip), 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_size_content);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(_getTextForSize(file.length()));

        tv = (TextView) ll.findViewById(R.id.dialog_detail_folder_subject);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_folder_content);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(file.getParent());

        tv = (TextView) ll.findViewById(R.id.dialog_detail_last_modified_time_subject);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_last_modified_time_content);
        tv.setPadding(0, 0, 0, (int)(10 * dip));
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(_getTextForDate(file.lastModified()));

        return sv;
    }

    private View _createVideoView(Context context, String path) {
        float dip =  ApiTheme.Dip();

        Bitmap  frame = null;
        Integer width = null;
        Integer height = null;
        Long    duration = null;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        String metadata = null;
        try {
            retriever.setDataSource(path);

            metadata = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            try {
                duration = Long.valueOf(metadata);
            } catch(Exception e) {
                e.printStackTrace();
            }
            metadata = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            try {
                width = Integer.valueOf(metadata);
            } catch(Exception e) {
                e.printStackTrace();
            }
            metadata = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            try {
                height = Integer.valueOf(metadata);
            } catch(Exception e) {
                e.printStackTrace();
            }
            frame = retriever.getFrameAtTime(duration > 1000 * 1000 ? duration : 1000 * 1000,
                                             MediaMetadataRetriever.OPTION_NEXT_SYNC);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }

        if(frame == null) {
            frame = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MINI_KIND);
        }
        if(frame != null) {
            frame = ApiBitmapFactory.createBitmapOnHeight(frame, (int)(64 * dip), 0);
        }

        File file = new File(path);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ScrollView sv = (ScrollView) inflater.inflate(R.layout.dialog_detail_video, null);
        sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LinearLayout ll = (LinearLayout) sv.findViewById(R.id.dialog_detail_layout);
        ll.setPadding((int)(10 * dip), (int)(15 * dip), (int)(10 * dip), (int)(5 * dip));

        TextView tv = null;
        tv = (TextView) ll.findViewById(R.id.dialog_detail_filename);
        tv.setPadding((int)(10 * dip), 0, (int)(10 * dip), 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailNameId());
        tv.setTextSize(DualListTheme.TextSize(18f));
        tv.setText(file.getName());

        if(frame != null) {
            tv.setCompoundDrawablePadding((int)(10 * dip));
            Drawable leftDrawable = null;
            if(frame != null) {
                leftDrawable = new BitmapDrawable(context.getResources(), frame);
                leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(), leftDrawable.getIntrinsicHeight());
            }
            if(frame.getWidth() / dip > 100) {
                tv.setCompoundDrawables(null, leftDrawable, null, null);
            } else {
                tv.setCompoundDrawables(leftDrawable, null, null, null);
            }
        }

        if(duration != null) {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_duration_subject);
            tv.setPadding(0, (int)(20 * dip), (int)(10 * dip), 0);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv = (TextView) ll.findViewById(R.id.dialog_detail_duration_content);
            tv.setPadding(0, (int)(20 * dip), 0, 0);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv.setText(_durationToStringMs(duration));
        } else {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_duration_subject);
            tv.setVisibility(View.GONE);
            tv = (TextView) ll.findViewById(R.id.dialog_detail_duration_content);
            tv.setVisibility(View.GONE);
        }

        if(width != null && height != null) {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_resolution_subject);
            tv.setPadding(0, (int)(7 * dip), (int)(10 * dip), 0);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv = (TextView) ll.findViewById(R.id.dialog_detail_resolution_content);
            tv.setPadding(0, (int)(7 * dip), 0, 0);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv.setText(String.format("%d x %d", width, height));
        } else {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_resolution_subject);
            tv.setVisibility(View.GONE);
            tv = (TextView) ll.findViewById(R.id.dialog_detail_resolution_content);
            tv.setVisibility(View.GONE);
        }

        tv = (TextView) ll.findViewById(R.id.dialog_detail_size_subject);
        tv.setPadding(0, (int)(7 * dip), (int)(10 * dip), 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_size_content);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(_getTextForSize(file.length()));

        tv = (TextView) ll.findViewById(R.id.dialog_detail_folder_subject);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_folder_content);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(file.getParent());

        tv = (TextView) ll.findViewById(R.id.dialog_detail_last_modified_time_subject);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_last_modified_time_content);
        tv.setPadding(0, 0, 0, (int)(10 * dip));
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(_getTextForDate(file.lastModified()));

        return sv;
    }

    private View _createMP3View(Context context, String path) {
        float dip =  ApiTheme.Dip();

        ContentResolver contentResolver = context.getContentResolver();
        String[] audioColumns = { MediaStore.Audio.Media.ALBUM,
                                  MediaStore.Audio.Media.ALBUM_ID,
                                  MediaStore.Audio.Media.ARTIST,
                                  MediaStore.Audio.Media.COMPOSER,
                                  MediaStore.Audio.Media.DISPLAY_NAME,
                                  MediaStore.Audio.Media.DURATION,
                                  MediaStore.Audio.Media.TITLE };

        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioColumns,
                MediaStore.Audio.Media.DATA + "='"+path+"'", null, null);

        String album = null;
        Long album_id = null;
        String artist = null;
        String composer = null;
        String display_name = null;
        Long duration = null;
        String title = null;
        Bitmap album_art = null;

        if (cursor.moveToFirst()) {
            album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            composer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
            display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));

            Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(artworkUri, album_id);


            InputStream in = null;
            try {
                in = contentResolver.openInputStream(uri);
                album_art = ApiBitmapFactory.createBitmapOnHeight(BitmapFactory.decodeStream(in), (int)(64 * dip), 0);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        File file = new File(path);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ScrollView sv = (ScrollView) inflater.inflate(R.layout.dialog_detail_mp3, null);
        sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LinearLayout ll = (LinearLayout) sv.findViewById(R.id.dialog_detail_layout);
        ll.setPadding((int)(10 * dip), (int)(15 * dip), (int)(10 * dip), (int)(5 * dip));

        TextView tv = null;
        tv = (TextView) ll.findViewById(R.id.dialog_detail_title);
        tv.setPadding((int)(10 * dip), 0, (int)(10 * dip), 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailNameId());
        tv.setTextSize(DualListTheme.TextSize(18f));
        tv.setText(title);

        if(album_art != null) {
            tv.setCompoundDrawablePadding((int)(10 * dip));
            Drawable leftDrawable = new BitmapDrawable(context.getResources(), album_art);
            leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(), leftDrawable.getIntrinsicHeight());
            if(album_art.getWidth() / dip > 100) {
                tv.setCompoundDrawables(null, leftDrawable, null, null);
            } else {
                tv.setCompoundDrawables(leftDrawable, null, null, null);
            }
        }

        tv = (TextView) ll.findViewById(R.id.dialog_detail_filename_subject);
        tv.setPadding(0, (int)(20 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_filename_content);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(file.getName());

        if(album != null) {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_album_subject);
            tv.setPadding(0, (int)(7 * dip), 0, 0);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv = (TextView) ll.findViewById(R.id.dialog_detail_album_content);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv.setText(album);
        } else {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_album_subject);
            tv.setVisibility(View.GONE);
            tv = (TextView) ll.findViewById(R.id.dialog_detail_album_content);
            tv.setVisibility(View.GONE);
        }

        if(artist != null) {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_artist_subject);
            tv.setPadding(0, (int)(7 * dip), 0, 0);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv = (TextView) ll.findViewById(R.id.dialog_detail_artist_content);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv.setText(artist);
        } else {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_artist_subject);
            tv.setVisibility(View.GONE);
            tv = (TextView) ll.findViewById(R.id.dialog_detail_artist_content);
            tv.setVisibility(View.GONE);
        }

        if(composer != null) {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_composer_subject);
            tv.setPadding(0, (int)(7 * dip), 0, 0);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv = (TextView) ll.findViewById(R.id.dialog_detail_composer_content);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv.setText(composer);
        } else {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_composer_subject);
            tv.setVisibility(View.GONE);
            tv = (TextView) ll.findViewById(R.id.dialog_detail_composer_content);
            tv.setVisibility(View.GONE);
        }

        if(duration != null) {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_duration_subject);
            tv.setPadding(0, (int)(7 * dip), (int)(10 * dip), 0);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv = (TextView) ll.findViewById(R.id.dialog_detail_duration_content);
            tv.setPadding(0, (int)(7 * dip), 0, 0);
            tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
            tv.setTextSize(DualListTheme.TextSize(16f));
            tv.setText(_durationToStringMs(duration));
        } else {
            tv = (TextView) ll.findViewById(R.id.dialog_detail_duration_subject);
            tv.setVisibility(View.GONE);
            tv = (TextView) ll.findViewById(R.id.dialog_detail_duration_content);
            tv.setVisibility(View.GONE);
        }

        tv = (TextView) ll.findViewById(R.id.dialog_detail_size_subject);
        tv.setPadding(0, (int)(7 * dip), (int)(10 * dip), 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_size_content);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(_getTextForSize(file.length()));

        tv = (TextView) ll.findViewById(R.id.dialog_detail_folder_subject);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_folder_content);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(file.getParent());

        tv = (TextView) ll.findViewById(R.id.dialog_detail_last_modified_time_subject);
        tv.setPadding(0, (int)(7 * dip), 0, 0);
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailTitleId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv = (TextView) ll.findViewById(R.id.dialog_detail_last_modified_time_content);
        tv.setPadding(0, 0, 0, (int)(10 * dip));
        tv.setTextAppearance(context, DualListTheme.TextApperranceDialogDetailContentId());
        tv.setTextSize(DualListTheme.TextSize(16f));
        tv.setText(_getTextForDate(file.lastModified()));

        return sv;
    }

    private String _durationToStringMs(long duration) {
        long d = duration / 1000;
        int hour = (int) (d / 3600);
        int min = (int) (d % 3600) / 60;
        int sec = (int) (d % 60);

        return String.format("%02d:%02d:%02d", hour, min, sec);
    }

    private String _getTextForSize(long size) {
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
        return text;
    }

    private String _getTextForDate(long milliseconds) {
        Date from = new Date(milliseconds);
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return transFormat.format(from);
    }
}
