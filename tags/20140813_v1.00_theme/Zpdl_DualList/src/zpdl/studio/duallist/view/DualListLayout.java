package zpdl.studio.duallist.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import zpdl.studio.api.util.ApiExternalStorage;
import zpdl.studio.duallist.DualListTheme;
import zpdl.studio.duallist.R;
import zpdl.studio.duallist.view.DualListView.onFileBrowserListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class DualListLayout extends ViewGroup {
    private static final int EVENT_NONE             = 0x0000;
    private static final int EVENT_DOWN             = 0x0001;
    private static final int EVENT_SCROLL           = 0x0002;
    private static final int EVENT_FLING            = 0x0003;

    private static final int FLING_CENTER           = 0x0001;
    private static final int FLING_LEFT             = 0x0002;
    private static final int FLING_RIGHT            = 0x0003;

    private Context             mContext;
    private boolean             mIsSelect;

    private DualListView  mFolderView;
    private DualListView  mFileView;
    private View             mFileBackGroundView;
    private TextView         mEmptyView;
    private ImageView        mBoundaryView;

    private onDualListLayoutListener    mListener;

    private String  mPath;
    private int     mEvent;

    private int mBoundaryWidth;
    private float mBoundaryPosf;
    private int mBoundaryListMin;
    private int mBoundaryListMax;
    private int mBoundaryListGap;

    private int mScrollBase;
    private long mTransitionLoopTime;

    private GestureDetector mGestureDetector;
    private int SCROLL_MIN_DISTANCE;
    private int FLING_MIN_DISTANCE;
    private int FLING_MAX_OFF_PATH;
    private int FLING_THRESHOLD_VELOCITY;
    private static final int FLING_DURATION = 300;

    public interface onDualListLayoutListener {
        void    onFolderChange(String path);
        void    onFileClick(String path);
        boolean onLongClick(DualListItem item);
        void    onUpdateMenu();
    }

    public DualListLayout(Context context) {
        this(context, null);
    }

    public DualListLayout(Context context, AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public DualListLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mIsSelect = false;
        mListener = null;

        mGestureDetector = new GestureDetector(mContext, mGestureListener);

        SCROLL_MIN_DISTANCE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, mContext.getResources().getDisplayMetrics());
        FLING_MIN_DISTANCE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics());
        FLING_MAX_OFF_PATH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, mContext.getResources().getDisplayMetrics());
        FLING_THRESHOLD_VELOCITY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, mContext.getResources().getDisplayMetrics());

        mBoundaryWidth = 0;
        mBoundaryPosf = 0.5f;
        mBoundaryListMin = 0;
        mBoundaryListMax = 0;
        mBoundaryListGap = 0;

        mScrollBase = 0;
        mTransitionLoopTime = 0;

        _init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        mBoundaryListMax = widthSize - mBoundaryListMin - mBoundaryWidth;
        mBoundaryListGap = mBoundaryListMax - mBoundaryListMin;

        int cWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundaryListMax, MeasureSpec.getMode(widthMeasureSpec));

        measureChild(mFolderView, cWidthMeasureSpec, heightMeasureSpec);
        measureChild(mBoundaryView, MeasureSpec.makeMeasureSpec(mBoundaryWidth, MeasureSpec.getMode(widthMeasureSpec)), heightMeasureSpec);
        measureChild(mFileBackGroundView, cWidthMeasureSpec, heightMeasureSpec);
        measureChild(mFileView, cWidthMeasureSpec, heightMeasureSpec);
        measureChild(mEmptyView, cWidthMeasureSpec, heightMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = l;
        int top = t;
        int right = r;
        int bottom = b;

        int height = b - t;

        left = 0;
        top = 0;
        right = left + mFolderView.getMeasuredWidth();
        bottom = top + height;
        mFolderView.layout(left, top, right, bottom);

        left = mBoundaryListMin;
        top = 0;
        right = left + mBoundaryView.getMeasuredWidth();
        bottom = top + height;
        mBoundaryView.layout(left, top, right, bottom);

        left = mBoundaryListMin + mBoundaryView.getMeasuredWidth();
        top = 0;
        right = left + mFileView.getMeasuredWidth();
        bottom = top + height;
        mFileBackGroundView.layout(left, top, right, bottom);
        mFileView.layout(left, top, right, bottom);
        mEmptyView.layout(left, top, right, bottom);

        _invalidatePosition();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            break;
        }

        return super.dispatchTouchEvent(ev);
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable p = super.onSaveInstanceState();

        Bundle bundle = new Bundle();
        if(mFolderView.getChildAt(0) != null) {
            bundle.putInt("folder_position", mFolderView.getFirstVisiblePosition());
            bundle.putInt("folder_scroll",  mFolderView.getChildAt(0).getTop());
        }

        if(mFileView.getChildAt(0) != null) {
            bundle.putInt("file_position",  mFileView.getFirstVisiblePosition());
            bundle.putInt("file_scroll",  mFileView.getChildAt(0).getTop());
        }
        bundle.putFloat("boundaryPos", mBoundaryPosf);

        bundle.putParcelable("parcelable_state", p);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        mFolderView.setSelectionFromTop(bundle.getInt("folder_position", 0),bundle.getInt("folder_scroll", 0));
        mFileView.setSelectionFromTop(bundle.getInt("file_position", 0),bundle.getInt("file_scroll", 0));
        mBoundaryPosf = bundle.getFloat("boundaryPos");

        super.onRestoreInstanceState(bundle.getParcelable("parcelable_state"));
    }

    public void setOnDualListLayoutListener(onDualListLayoutListener l) {
        mListener = l;
    }

    private String   mSdCard;
    private String[] mExtsdCard;
    private String   mRoot;

    public void init(String path, boolean center) {
        mPath = path;
        String parentPath = mPath.lastIndexOf(File.separator) > 0 ?
                            mPath.substring(0, mPath.lastIndexOf(File.separator)) : null;


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
            mFolderView.initSDCard(sdcard);
            mFileView.init();
        } else {
            ArrayList<String> folderList = new ArrayList<String>();
            ArrayList<String> fileList = new ArrayList<String>();

            File[] files = new File(mPath).listFiles();
            if(files != null) {
                for(File file: files) {
                    if(file.isHidden()) {
                        continue;
                    } else if(file.isDirectory()) {
                        folderList.add(file.getPath());
                    } else if(file.isFile()) {
                        fileList.add(file.getPath());
                    }
                }
            }

            if(folderList.size() > 0) {
                Collections.sort(folderList, String.CASE_INSENSITIVE_ORDER);
                String[] listParam = new String[folderList.size()];
                for(int i = 0; i < folderList.size(); i++) {
                    listParam[i] = folderList.get(i);
                }
                mFolderView.initFileList(listParam, parentPath);
            } else {
                mFolderView.initFileList(null, parentPath);
            }

            if(fileList.size() > 0) {
                Collections.sort(fileList, String.CASE_INSENSITIVE_ORDER);
                String[] listParam = new String[fileList.size()];
                for(int i = 0; i < fileList.size(); i++) {
                    listParam[i] = fileList.get(i);
                }
                mFileView.initFileList(listParam, null);
            } else {
                mFileView.init();
            }
        }

        if(center) {
            _transitionStart(FLING_CENTER);
        }
        if(mListener != null) {
            mListener.onFolderChange(mPath);
        }
        _invalidate();
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

    public void modeBase() {
        if(mIsSelect) {
            mIsSelect = false;
            mFileView.setSelectMode(false);
            mFolderView.setSelectMode(false);
            _invalidate();
            if(mListener != null) mListener.onUpdateMenu();
        }
    }

    public void modeSelect() {
        if(!mIsSelect) {
            mIsSelect = true;
            mFolderView.setSelectMode(true);
            mFileView.setSelectMode(true);
            _invalidate();

            if(mListener != null) mListener.onUpdateMenu();
        }
    }

    public boolean isSelectMode() {
        return mIsSelect;
    }

    public void setFileSelectAll(boolean b) {
        mFolderView.setSelectAll(b);
        mFileView.setSelectAll(b);
        _invalidate();
    }

    public boolean isFileSelectAll() {
        return mFolderView.isSelectAll() && mFileView.isSelectAll();
    }

    public int getFileSelectCount() {
        return mFolderView.getSelectCount() + mFileView.getSelectCount();
    }

    public int getFileSelectMax() {
        return mFolderView.getSelectMax() + mFileView.getSelectMax();
    }

    public String getPath() {
        return mPath;
    }

    public String[] getFileSelectArray() {
        ArrayList<String> al = new ArrayList<String>();

        if(mFolderView.isSelectEnable()) {
            al.addAll(mFolderView.getSelectList());
        }
        if(mFileView.isSelectEnable()) {
            al.addAll(mFileView.getSelectList());
        }
        String[] fl = new String[al.size()];

        for(int i = 0; i < al.size(); i++) {
            fl[i] = al.get(i);
        }

        return fl;
    }

    public View getForderView() {
        return mFolderView;
    }

    public View getFileView() {
        return mFileView;
    }

    public boolean modeBack() {
        if(mIsSelect) {
            modeBase();
            return true;
        }

        return false;
    }

    public void addFolder(String folder) {
        ArrayList<String> folderList = new ArrayList<String>();
        folderList.add(folder);

        mFolderView.add(folderList);
        mFolderView.smoothScrollToPosition(mFolderView.getCount());
        _invalidate();
    }

    public void rename(String source, String target) {
        mFolderView.rename(source, target);
        mFileView.rename(source, target);
        _invalidate();
    }

    private void _init() {
        _initRoot();

        mPath = null;

        mBoundaryListMin = (int) (DualListRowView.getIconWidth(mContext, DualListTheme.Scale() * 0.90f));

        mFolderView = new DualListView(mContext);
        mFolderView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mFolderView.setBackgroundResource(DualListTheme.BackgoundId());
        mFolderView.setSelector(DualListTheme.SelectorListDrawableId());
        mFolderView.setonFileBrowserListener(mFolderListener);
        addView(mFolderView);

        mBoundaryWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());

        mBoundaryView = new ImageView(mContext);
        mBoundaryView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mBoundaryView.setImageResource(DualListTheme.DualListBoundaryId());
        mBoundaryView.setScaleType(ScaleType.FIT_XY);
        addView(mBoundaryView);

        mFileBackGroundView = new View(mContext);
        mFileBackGroundView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mFileBackGroundView.setBackgroundResource(DualListTheme.BackgoundId());
        addView(mFileBackGroundView);

        float textSize = 20f;
        int smallestScreenWidthDp = mContext.getResources().getConfiguration().smallestScreenWidthDp;
        if(smallestScreenWidthDp >= 800) {
            textSize = 30f;
        } else if(smallestScreenWidthDp >= 600) {
            textSize = 25f;
        }
        mEmptyView = new TextView(mContext);
        mEmptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mEmptyView.setBackgroundResource(DualListTheme.BackgoundId());
        mEmptyView.setClickable(true);
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setTextSize(textSize);
        mEmptyView.setTextColor(0xFF040404);
        mEmptyView.setText(R.string.File_is_empty);
        addView(mEmptyView);

        mFileView = new DualListView(mContext);
        mFileView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mFileView.setBackgroundResource(DualListTheme.BackgoundId());
        mFileView.setSelector(DualListTheme.SelectorListDrawableId());
        mFileView.setEmptyView(mEmptyView);
        mFileView.setonFileBrowserListener(mFileListener);
        addView(mFileView);
    }

    private void _invalidate() {
        requestLayout();
    }

    private void _invalidatePosition() {
        if(mBoundaryPosf < 0) {
            mBoundaryPosf = 0;
        } else if(mBoundaryPosf > 1) {
            mBoundaryPosf = 1;
        }
        int boundaryPos = (int)(mBoundaryPosf * mBoundaryListGap);
        float scaleD = mBoundaryPosf * 0.10f;

        mFolderView.setScaleX(0.90f + scaleD);
        mFolderView.setScaleY(0.90f + scaleD);
        mFolderView.setTranslationX(-(mFolderView.getMeasuredWidth() * (0.10f - scaleD) / 2));

        mBoundaryView.setScaleX(1.00f - scaleD);
        mBoundaryView.setTranslationX(boundaryPos);

        mFileBackGroundView.setTranslationX(boundaryPos);

        mFileView.setScaleX(1.00f - scaleD);
        mFileView.setScaleY(1.00f - scaleD);
        mFileView.setTranslationX(boundaryPos - (mEmptyView.getMeasuredWidth() * scaleD / 2));

        mEmptyView.setScaleX(1.00f - scaleD);
        mEmptyView.setScaleY(1.00f - scaleD);
        mEmptyView.setTranslationX(boundaryPos - (mEmptyView.getMeasuredWidth() * scaleD / 2));
    }

    private void _transitionStart(int msg) {
        mEvent = EVENT_FLING;
        mTransitionLoopTime = System.currentTimeMillis();

        mHandlerTransition.sendEmptyMessage(msg);
    }

    private onFileBrowserListener mFolderListener = new onFileBrowserListener() {
        @Override
        public void onClick(DualListItem item) {
            if(!mIsSelect) {
                init(item.getPath(), false);
            }
            if(mListener != null) mListener.onUpdateMenu();
        }

        @Override
        public boolean onLongClick(DualListItem item) {
            if(mListener != null) return mListener.onLongClick(item);
            return false;
        }
    };

    private onFileBrowserListener mFileListener = new onFileBrowserListener() {
        @Override
        public void onClick(DualListItem item) {
            if(mIsSelect) {
                if(mListener != null) mListener.onUpdateMenu();
            } else {
                if(mListener != null) mListener.onFileClick(item.getPath());
            }
        }

        @Override
        public boolean onLongClick(DualListItem item) {
            if(mListener != null) return mListener.onLongClick(item);
            return false;
        }
    };

    private OnGestureListener mGestureListener = new OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            if(mFileView.getLeft() + mFileView.getTranslationX() < e.getX()) {
                mEvent = EVENT_DOWN;
            } else {
                mEvent = EVENT_NONE;
            }
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(mEvent == EVENT_DOWN) {
                if (Math.abs(e1.getY() - e2.getY()) > FLING_MAX_OFF_PATH) {
                    mEvent = EVENT_NONE;
                    return false;
                }

                if(Math.abs(e1.getX() - e2.getX()) > SCROLL_MIN_DISTANCE) {
                    mEvent = EVENT_SCROLL;
                    mScrollBase = (int)(e2.getX() - e1.getX());
                }
            }

            if(mEvent == EVENT_SCROLL) {
                if(mFileView.isPressed()) {
                    MotionEvent cancel_event = MotionEvent.obtain(
                            SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    mFileView.dispatchTouchEvent(cancel_event);
                } else if(mFolderView.isPressed()) {
                    MotionEvent cancel_event = MotionEvent.obtain(
                            SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    mFolderView.dispatchTouchEvent(cancel_event);
                }
                mBoundaryPosf = mBoundaryPosf + (e2.getX() - e1.getX() - mScrollBase) / mBoundaryListGap;
                mScrollBase = (int)(e2.getX() - e1.getX());
                _invalidatePosition();
            }

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mEvent == EVENT_NONE || Math.abs(e1.getY() - e2.getY()) > FLING_MAX_OFF_PATH) {
                return false;
            }

            if(e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_THRESHOLD_VELOCITY) {
                _transitionStart(FLING_LEFT);
            } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_THRESHOLD_VELOCITY) {
                _transitionStart(FLING_RIGHT);
            }
            return false;
        }
    };

    private Handler mHandlerTransition = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mEvent != EVENT_FLING) {
                return;
            }

            long loopTime = System.currentTimeMillis();
            float delay = loopTime - mTransitionLoopTime;
            mTransitionLoopTime = loopTime;

            switch (msg.what) {
                case FLING_CENTER: {
                    if(mBoundaryPosf < 0.5f) {
                        mBoundaryPosf += delay / (float) FLING_DURATION;
                        if(mBoundaryPosf < 0.5f) {
                            mHandlerTransition.sendEmptyMessage(FLING_CENTER);
                        } else {
                            mBoundaryPosf = 0.5f;
                        }
                        _invalidatePosition();
                    } else if(mBoundaryPosf > 0.5f) {
                        mBoundaryPosf -= delay / (float) FLING_DURATION;
                        if(mBoundaryPosf > 0.5f) {
                            mHandlerTransition.sendEmptyMessage(FLING_CENTER);
                        } else {
                            mBoundaryPosf = 0.5f;
                        }
                        _invalidatePosition();
                    }
                }
                break;

                case FLING_LEFT: {
                    mBoundaryPosf -= delay / (float) FLING_DURATION;
                    _invalidatePosition();
                    if(mBoundaryPosf > 0) {
                        mHandlerTransition.sendEmptyMessage(FLING_LEFT);
                    }
                }
                break;

                case FLING_RIGHT: {
                    mBoundaryPosf += delay / (float) FLING_DURATION;
                    _invalidatePosition();
                    if(mBoundaryPosf < 1) {
                        mHandlerTransition.sendEmptyMessage(FLING_RIGHT);
                    }
                }
                break;
            }
        }
    };
}
