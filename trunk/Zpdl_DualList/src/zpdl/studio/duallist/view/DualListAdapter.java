package zpdl.studio.duallist.view;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import zpdl.studio.api.dcache.DCache;
import zpdl.studio.api.dcache.DCacheData;
import zpdl.studio.api.dcache.DCacheInteface;
import zpdl.studio.api.dcache.DCacheParam;
import zpdl.studio.api.drawable.ApiBitmapFactory;
import zpdl.studio.api.drawable.ApiDrawableConfig;
import zpdl.studio.api.drawable.ApiDrawableFactory;
import zpdl.studio.api.drawable.ApiTextBitmapFactory;
import zpdl.studio.api.util.ApiLog;
import zpdl.studio.duallist.R;
import zpdl.studio.duallist.util.DualListDCacheData;
import zpdl.studio.duallist.util.DualListDCacheImage;
import zpdl.studio.duallist.util.DualListDCacheMP3;
import zpdl.studio.duallist.util.DualListDCacheParam;
import zpdl.studio.duallist.util.DualListDCacheVideo;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

public class DualListAdapter extends ArrayAdapter<DualListItem> {
    private final Bitmap BITMAP_UNKNOWN;
    private final Bitmap BITMAP_UP;
    private final Bitmap BITMAP_SDCARD;
    private final Bitmap BITMAP_EXTSDCARD;
    private final Bitmap BITMAP_FOLDER;
    private final Bitmap BITMAP_SUB_VIDEO;
    private final Bitmap BITMAP_SUB_IMAGE;
    private final Bitmap BITMAP_SUB_SELETALL_ENABLE;
    private final Bitmap BITMAP_SUB_SELETALL_DISABLE;

    private Context mContext;
    private Resources mResources;
    private ContentResolver mContentResolver;
    private PackageManager mPackageManager;

    private ApiTextBitmapFactory mTextBitmapFactory;

    private DCache  mDCache;
    private int     mMainWidth;
    private int     mMainHeight;

    private boolean mSelect;

    private ApiDrawableConfig mDrawableConfig;

    public void setSelectMode(boolean s) {
        if(mSelect != s) {
            mSelect = s;
            notifyDataSetInvalidated();
        }
    }

    public boolean getSelectMode() {
        return mSelect;
    }

    public int doSelect(DualListItem item, View v) {
        if(item.isPosiibleSelect()) {
            if(item.getSelect()) {
                item.setSelect(false);
                ((DualListRowView)v).setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
                return -1;
            } else {
                item.setSelect(true);
                ((DualListRowView)v).setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
                return 1;
            }
        }
        return 0;
    }

    public DualListAdapter(Context context, ArrayList<DualListItem> objects) {
        super(context, 0, objects);

        mContext = context;
        mResources = mContext.getResources();
        mContentResolver = mContext.getContentResolver();
        mPackageManager = mContext.getPackageManager();

        mSelect = false;

        mDrawableConfig = new ApiDrawableConfig(mContext.getResources());

        int textSize = 15;
        int smallestScreenWidthDp = context.getResources().getConfiguration().smallestScreenWidthDp;
        if(smallestScreenWidthDp >= 800) {
            textSize = 25;
        } else if(smallestScreenWidthDp >= 600) {
            textSize = 20;
        }
        mTextBitmapFactory = new ApiTextBitmapFactory(30, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                                                                                       textSize,
                                                                                       mResources.getDisplayMetrics()));

        mMainWidth = DualListRowView.getMainIconWidth(mContext, mDrawableConfig.scale);
        mMainHeight = DualListRowView.getMainIconHeight(mContext, mDrawableConfig.scale);

        BITMAP_UNKNOWN = ApiDrawableFactory.getBitmap(mContext.getResources(), mDrawableConfig, R.drawable.kh_duallist_ic_unknown_file, mMainWidth, mMainHeight);
        BITMAP_UP = ApiDrawableFactory.getBitmap(mContext.getResources(), mDrawableConfig, R.drawable.kh_duallist_ic_folder, mMainWidth, mMainHeight);
        BITMAP_SDCARD = ApiDrawableFactory.getBitmap(mContext.getResources(), mDrawableConfig, R.drawable.kh_duallist_ic_folder, mMainWidth, mMainHeight);
        BITMAP_EXTSDCARD = ApiDrawableFactory.getBitmap(mContext.getResources(), mDrawableConfig, R.drawable.kh_duallist_ic_folder, mMainWidth, mMainHeight);
        BITMAP_FOLDER = ApiDrawableFactory.getBitmap(mContext.getResources(), mDrawableConfig, R.drawable.kh_duallist_ic_folder, mMainWidth, mMainHeight);
        BITMAP_SUB_VIDEO = ApiDrawableFactory.getBitmap(mContext.getResources(), mDrawableConfig, R.drawable.kh_duallist_ic_sub_video, mMainWidth/2, mMainHeight/2);
        BITMAP_SUB_IMAGE = ApiDrawableFactory.getBitmap(mContext.getResources(), mDrawableConfig, R.drawable.kh_duallist_ic_sub_image, mMainWidth/2, mMainHeight/2);
        BITMAP_SUB_SELETALL_ENABLE = ApiDrawableFactory.getBitmap(mContext.getResources(), mDrawableConfig, R.drawable.kh_duallist_ic_select_true, mMainWidth/2, mMainHeight/2);
        BITMAP_SUB_SELETALL_DISABLE = ApiDrawableFactory.getBitmap(mContext.getResources(), mDrawableConfig, R.drawable.kh_duallist_ic_select_false, mMainWidth/2, mMainHeight/2);

        final int largeMemClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getLargeMemoryClass();
        final int largeDiskSize = 1024 * 1024 * largeMemClass / 8;
        final int largeCacheSize = 1024  * 1024 * largeMemClass / 32;

        mDCache = new DCache(context, "FileBrowserDCache_Kh", largeCacheSize, largeDiskSize, new DCacheInteface() {
            @Override
            public boolean write(DCacheData d, BufferedOutputStream out) throws IOException {
                d.write(out);
                return true;
            }

            @Override
            public DCacheData read(BufferedInputStream in) throws IOException {
                return DualListDCacheData.readData(in);
            }

            @Override
            public DCacheData load(DCacheParam p) {
                DualListDCacheParam param = (DualListDCacheParam) p;
                DCacheData data = null;

                switch(param.getType()) {
                    case DualListItem.VIDEO : {
                        data = (DCacheData) _loadVideoData(param);
                    } break;
                    case DualListItem.IMAGE : {
                        data = (DCacheData) _loadImageData(param);
                    } break;
                    case DualListItem.MP3 : {
                        data = (DCacheData) _loadMP3Data(param);
                    } break;
                }

                return data;
            }

            @Override
            public void done(DCacheParam p) {
                DualListDCacheParam param = (DualListDCacheParam) p;

                DualListRowView view = param.getView();
                if(view.getPath().equals(param.getPath())) {
                    switch(((DualListDCacheData) param.getDCacheData()).getType()) {
                        case DualListItem.VIDEO : {
                            DualListDCacheVideo data = (DualListDCacheVideo) param.getDCacheData();

                            if(data.getThumbnail() != null) {
                                view.setMainIcon(data.getThumbnail());
                                view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_VIDEO));
                            }
                            view.setInfoExt(_Video_Ext_Message(param.getPath(), data.getDuration(), data.getWidth(), data.getHeight()));
                        } break;
                        case DualListItem.IMAGE : {
                            DualListDCacheImage data = (DualListDCacheImage) param.getDCacheData();

                            if(data.getThumbnail() != null) {
                                view.setMainIcon(data.getThumbnail());
                                view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_IMAGE));
                            }
                            view.setInfoExt(_Image_Ext_Message(param.getPath(), data.getWidth(), data.getHeight()));
                        } break;
                        case DualListItem.MP3 : {
                            if (param.getDCacheData() instanceof DualListDCacheMP3) {
                                DualListDCacheMP3 data = (DualListDCacheMP3) param.getDCacheData();

                                if(data.getThumbnail() != null) {
                                    view.setMainIcon(data.getThumbnail());
                                    view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_IMAGE));
                                }
                                view.setInfoExt(_MP3_Ext_Message(param.getPath(), data.getArtist(), data.getDuration()));
                            } else {
                                ApiLog.i("param.getDCacheData() = %d", ((DualListDCacheData) param.getDCacheData()).getType());
                            }

                        } break;
                    }
                }

            }});
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DualListRowView view = (DualListRowView) convertView;

        if(view == null) {
            view = new DualListRowView(mContext);
            view.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

        final DualListItem data = this.getItem(position);

        if(data != null) {
            view.setPath(data.getPath());
            int type = data.getType(mPackageManager);

            switch(type) {
                case DualListItem.SUPPORT_NOT : {
                    _viewWrapper_NOTSUPPORT(view, data);
                } break;

                case DualListItem.SUPPORT_UNKNOW : {
                    _viewWrapper_SUPPORT_UNKNOWN(view, data);
                } break;

                case DualListItem.UP : {
                    _viewWrapper_UP(view, data);
                } break;

                case DualListItem.SDCARD : {
                    _viewWrapper_SDCARD(view, data);
                } break;

                case DualListItem.EXTSDCARD : {
                    _viewWrapper_EXTSDCARD(view, data);
                } break;

                case DualListItem.FOLDER : {
                    _viewWrapper_FOLDER(view, data);
                } break;

                case DualListItem.TEXT_PLAIN :
                case DualListItem.TEXT_XML :
                case DualListItem.TEXT_X : {
                    _viewWrapper_TEXT(view, data);
                } break;

                case DualListItem.TEXT_PDF : {
                    _viewWrapper_PDF(view, data);
                } break;

                case DualListItem.TEXT_WORD : {
                    _viewWrapper_WORD(view, data);
                } break;

                case DualListItem.TEXT_EXCEL : {
                    _viewWrapper_EXECL(view, data);
                } break;

                case DualListItem.TEXT_POWERPOINT : {
                    _viewWrapper_POWERPOINT(view, data);
                } break;

                case DualListItem.SOUND : {
                    _viewWrapper_SOUND(view, data);
                } break;

                case DualListItem.MP3 : {
                    _viewWrapper_MP3(view, data);
                } break;

                case DualListItem.IMAGE : {
                    _viewWrapper_IMAGE(view, data);
                } break;

                case DualListItem.VIDEO : {
                    _viewWrapper_VIDEO(view, data);
                } break;

                 case DualListItem.INTERNET : {
                     _viewWrapper_INTERNET(view, data);
                } break;

                case DualListItem.ZIP_FILE : {
                    _viewWrapper_ZIPFILE(view, data);
                } break;

                case DualListItem.CERTIFICATE : {
                    _viewWrapper_CERTIFICATE(view, data);
                } break;

                case DualListItem.CALENDAR : {
                    _viewWrapper_CALENDAR(view, data);
                } break;

                case DualListItem.NAME_CARD : {
                    _viewWrapper_NAMECARD(view, data);
                } break;

                case DualListItem.ANDROID_APPLICATION : {
                    _viewWrapper_ANDROID_APPLICATION(view, data);
                } break;

                case DualListItem.GOOGLE_EARTH : {
                    _viewWrapper_GOOGLE_EARTH(view, data);
                } break;

                default :
                    _viewWrapper_NOTSUPPORT(view, data);

            }
        }
        return view;
    }

    private void _viewWrapper_UP(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_UP));
        view.setSubIconGone();
        view.setName(item.getName());
        view.setInfoGone();
        view.setClickableGone();
    }

    private void _viewWrapper_SDCARD(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SDCARD));
        view.setSubIconGone();
        view.setName(item.getName());
        view.setInfoGone();
        view.setClickableGone();
    }

    private void _viewWrapper_EXTSDCARD(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_EXTSDCARD));
        view.setSubIconGone();
        view.setName(item.getName());
        view.setInfoGone();
        view.setClickableGone();
    }

    private void _viewWrapper_FOLDER(DualListRowView view, final DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_FOLDER));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubIconGone();
        }
        view.setName(item.getName());
        view.setInfoGone();
        view.setClickableGone();
    }

    private void _viewWrapper_NOTSUPPORT(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_UNKNOWN));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get("?"/*item.getExtention()*/)));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(false);
    }

    private void _viewWrapper_SUPPORT_UNKNOWN(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_UNKNOWN));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_TEXT(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("TEXT")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_PDF(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("PDF")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_WORD(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("WORD")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_EXECL(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("EXECL")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_POWERPOINT(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("POPWER POINT")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_SOUND(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("SOUND")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_MP3(DualListRowView view, DualListItem item) {
        DualListDCacheParam param = new DualListDCacheParam(item, view);
        DualListDCacheData data = (DualListDCacheData) mDCache.load(param);
        Bitmap subBitmap = null;

        if(data == null || data.getType() != DualListItem.MP3) {
            view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_IMAGE));
            view.setInfoSize(item.getSize());
            view.setInfoExt(item.getExtention());
            subBitmap = mTextBitmapFactory.get(item.getExtention());
        } else {
            DualListDCacheMP3 imageData = (DualListDCacheMP3) data;
            view.setMainIcon(imageData.getThumbnail());
            view.setInfoSize(item.getSize());
            view.setInfoExt(_MP3_Ext_Message(item.getPath(), imageData.getArtist(), imageData.getDuration()));

            subBitmap = BITMAP_SUB_IMAGE;
        }

        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, subBitmap));
        }
        view.setName(item.getName());
        view.setClickable(true);
    }

    private String _MP3_Ext_Message(String path, String artist, Long duration) {
        StringBuilder sb = new StringBuilder();
        if(artist != null) {
            sb.append(artist);
        }
        if(duration != null) {
            sb.append("  "+_durationToStringMs(duration));
        }
        if(path != null) {
            sb.append("  "+DualListItem.getExtention(path));
        }
        return sb.toString();
    }

    private void _viewWrapper_IMAGE(DualListRowView view, DualListItem item) {
        DualListDCacheParam param = new DualListDCacheParam(item, view);
        DualListDCacheData data = (DualListDCacheData) mDCache.load(param);
        Bitmap subBitmap = null;

        if(data == null || data.getType() != DualListItem.IMAGE) {
            view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_IMAGE));
            view.setInfoSize(item.getSize());
            view.setInfoExt(item.getExtention());
            subBitmap = mTextBitmapFactory.get(item.getExtention());
        } else {
            DualListDCacheImage imageData = (DualListDCacheImage) data;
            view.setMainIcon(imageData.getThumbnail());
            view.setInfoSize(item.getSize());
            view.setInfoExt(_Image_Ext_Message(item.getPath(), imageData.getWidth(), imageData.getHeight()));

            subBitmap = BITMAP_SUB_IMAGE;
        }

        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, subBitmap));
        }
        view.setName(item.getName());
        view.setClickable(true);
    }

    private String _Image_Ext_Message(String path, Integer width, Integer height) {
        StringBuilder sb = new StringBuilder();
        if(width != null && height != null) {
            sb.append(String.format("%dx%d", width, height));
        }
        if(path != null) {
            sb.append("  "+DualListItem.getExtention(path));
        }
        return sb.toString();
    }

    private void _viewWrapper_VIDEO(DualListRowView view, DualListItem item) {
        DualListDCacheParam param = new DualListDCacheParam(item, view);
        DualListDCacheData  data = (DualListDCacheData) mDCache.load(param);
        Bitmap subBitmap = null;

        if(data == null || data.getType() != DualListItem.VIDEO) {
            view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_VIDEO));
            view.setInfoSize(item.getSize());
            view.setInfoExt(item.getExtention());
            subBitmap = mTextBitmapFactory.get(item.getExtention());
        } else {
            DualListDCacheVideo videoData = (DualListDCacheVideo) data;

            view.setMainIcon(videoData.getThumbnail());
            view.setInfoSize(item.getSize());
            view.setInfoExt(_Video_Ext_Message(item.getPath(), videoData.getDuration(), videoData.getWidth(), videoData.getHeight()));

            subBitmap = BITMAP_SUB_VIDEO;
        }

        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, subBitmap));
        }
        view.setName(item.getName());
        view.setClickable(true);
    }

    private String _Video_Ext_Message(String path, Long duration, Integer width, Integer height) {
        StringBuilder sb = new StringBuilder();
        if(duration != null) {
            sb.append(_durationToStringMs(duration));
        }
        if(width != null && height != null) {
            sb.append(String.format("  %dx%d", width, height));
        }
        if(path != null) {
            sb.append("  "+DualListItem.getExtention(path));
        }
        return sb.toString();
    }

    private void _viewWrapper_INTERNET(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("INTERNET")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_ZIPFILE(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("ZIP FILE")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_CERTIFICATE(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("CERTIFICATE")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_CALENDAR(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("CALENDAR")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_NAMECARD(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("NAME CARD")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_ANDROID_APPLICATION(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("ANDROID APPLICATION")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private void _viewWrapper_GOOGLE_EARTH(DualListRowView view, DualListItem item) {
        view.setMainDrawable(ApiDrawableFactory.getDrawable(mResources, mTextBitmapFactory.get("GOOGLE EARTH")));
        if(mSelect && item.getSelect()) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_ENABLE));
        } else if(mSelect) {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources, BITMAP_SUB_SELETALL_DISABLE));
        } else {
            view.setSubDrawable(ApiDrawableFactory.getDrawable(mResources,
                                                              mTextBitmapFactory.get(item.getExtention())));
        }
        view.setName(item.getName());
        view.setInfoSize(item.getSize());
        view.setInfoExt(item.getExtention());

        view.setClickable(true);
    }

    private DualListDCacheData _loadVideoData(DualListDCacheParam p) {
        Bitmap  frame = null;
        Integer width = null;
        Integer height = null;
        Long    duration = null;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        String metadata = null;
        try {
            retriever.setDataSource(p.getPath());

            metadata = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            try {
                duration = Long.valueOf(metadata);
            } catch(Exception e) {
                e.printStackTrace();
            }

            try {
                frame = retriever.getFrameAtTime(1000*1000, MediaMetadataRetriever.OPTION_NEXT_SYNC);
                if(frame != null) {
                    width = Integer.valueOf(frame.getWidth());
                    height = Integer.valueOf(frame.getHeight());
                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        frame = ApiBitmapFactory.createBitmapOnRect(frame, mMainWidth, mMainHeight, _getVideoRotation(retriever));
                    } else {
                        frame = ApiBitmapFactory.createBitmapOnRect(frame, mMainWidth, mMainHeight, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return new DualListDCacheVideo(frame, width, height, duration);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int _getVideoRotation(MediaMetadataRetriever r) {
        int rotation = 0;
        String rot = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if(rot != null) rotation = Integer.valueOf(rot);

        return rotation;
    }

    private DualListDCacheData _loadImageData(DualListDCacheParam p) {
        Integer width = null;
        Integer height = null;
        Bitmap thumbnail = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        int degree = 0;
        try {
            ExifInterface exif = new ExifInterface(p.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                    break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(p.getPath(), options);

        if(degree == 90 || degree == 270) {
            width = Integer.valueOf(options.outHeight);
            height = Integer.valueOf(options.outWidth);
        } else {
            width = Integer.valueOf(options.outWidth);
            height = Integer.valueOf(options.outHeight);
        }

        thumbnail = ApiBitmapFactory.createBitmapOnRectFast(p.getPath(), mMainWidth, mMainHeight);

        return new DualListDCacheImage(thumbnail, width, height);
    }

//    private FileBrowserDCacheData _loadMP3Data(FileBrowserDCacheParam p) {
//        Bitmap coverImage = null;
//        Long duration = null;
//        String artist = null;
//
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        try {
//            String metadata = null;
//            retriever.setDataSource(p.getPath());
//
//            try {
//                metadata = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//                duration = Long.valueOf(metadata);
//            } catch(Exception e) {
//                e.printStackTrace();
//            }
//
//            try {
//                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
//            } catch(Exception e) {
//                e.printStackTrace();
//            }
//            KhLog.i("duration = " + metadata);
//            KhLog.i("Artist = " + artist);
//            byte[] coverBytes =  retriever.getEmbeddedPicture();
//            if(coverBytes != null) {
//                InputStream is = new ByteArrayInputStream(coverBytes);
//                coverImage = KhBitmapFactory.createBitmapOnRect(BitmapFactory.decodeStream(is), mMainWidth, mMainHeight, 0);
//            }
//        } catch(Exception e) {
//            e.printStackTrace();
//        } finally {
//            retriever.release();
//        }
//        return new FileBrowserDCacheMP3(coverImage, duration, artist);
//    }

    private DualListDCacheData _loadMP3Data(DualListDCacheParam p) {
        Long    album_id = null;
        Bitmap  album_art = null;
        Long    duration = null;
        String  artist = null;

        String[] audioColumns = { MediaStore.Audio.Media.ALBUM_ID,
                                  MediaStore.Audio.Media.ARTIST,
                                  MediaStore.Audio.Media.DURATION
                                };

        Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioColumns,
                MediaStore.Audio.Media.DATA + "='"+p.getPath()+"'", null, null);

        if(cursor.moveToFirst()) {
            album_id = Long.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
            artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            duration = Long.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

            Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(artworkUri, album_id);
            InputStream in = null;
            try {
                in = mContentResolver.openInputStream(uri);
                album_art = ApiBitmapFactory.createBitmapOnRect(
                                BitmapFactory.decodeStream(in),
                                mMainWidth, mMainHeight, 0);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(in != null) in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//        String[] audioColumns = { MediaStore.Audio.Media.ALBUM,
//                                  MediaStore.Audio.Media.ALBUM_ID,
//                                  MediaStore.Audio.Media.ARTIST,
//                                  MediaStore.Audio.Media.BOOKMARK,
//                                  MediaStore.Audio.Media.COMPOSER,
//                                  MediaStore.Audio.Media.DISPLAY_NAME,
//                                  MediaStore.Audio.Media.DURATION,
//                                  MediaStore.Audio.Media.TITLE };
//
//        Cursor cursor = mContentResolver.query(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioColumns,
//                MediaStore.Audio.Media.DATA + "='"+p.getPath()+"'", null, null);
//        if (cursor.moveToFirst()) {
//            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
//            Long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//            String composer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
//            String display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
//            Long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
//            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
//
//            Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
//            Uri uri = ContentUris.withAppendedId(artworkUri, album_id);
//
//            Bitmap album_art = null;
//            InputStream in = null;
//            try {
//                 in = mContentResolver.openInputStream(uri);
//                 album_art = KhBitmapFactory.createBitmapOnRect(BitmapFactory.decodeStream(in), mMainWidth, mMainHeight, 0);
//            } catch (FileNotFoundException e) {
//                      e.printStackTrace();
//            } finally {
//                try {
//                    in.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        return new DualListDCacheMP3(album_art, duration, artist);
    }

    private String _durationToStringMs(long duration) {
        long d = duration / 1000;
        int hour = (int) (d / 3600);
        int min = (int) (d % 3600) / 60;
        int sec = (int) (d % 60);

        return String.format("%02d:%02d:%02d", hour, min, sec);
    }
}
