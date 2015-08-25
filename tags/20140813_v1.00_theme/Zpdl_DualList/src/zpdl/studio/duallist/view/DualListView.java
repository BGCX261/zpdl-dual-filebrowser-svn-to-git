package zpdl.studio.duallist.view;

import java.util.ArrayList;

import zpdl.studio.api.util.ApiLog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class DualListView extends ListView implements AdapterView.OnItemClickListener {
    private Context                         mContext;
    private onFileBrowserListener           mListener;

    private ArrayList<DualListItem>      mList;
    private DualListAdapter              mAdapter;

    private int                             mSelectCnt;
    private int                             mSelectMax;

    public interface onFileBrowserListener {
        void    onClick(DualListItem item);
        boolean onLongClick(DualListItem item);
    }

    public void setonFileBrowserListener(onFileBrowserListener l) {
        mListener = l;
    }

    public DualListView(Context context) {
        this(context, null);
    }

    public DualListView(Context context, AttributeSet attrs) {
        this(context, attrs , android.R.attr.absListViewStyle);
    }

    public DualListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        mList       = null;
        mAdapter    = null;

        mListener   = null;

        mSelectCnt  = 0;
        mSelectMax  = 0;

        mList = new ArrayList<DualListItem>();
        mAdapter = new DualListAdapter(mContext, mList);

        this.setAdapter(mAdapter);
        this.setOnItemClickListener(this);
        this.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                DualListItem item = (DualListItem) parent.getItemAtPosition(position);

                if(mAdapter.getSelectMode()) {

                } else {
                    if(item.getType() == DualListItem.SDCARD ||
                       item.getType() == DualListItem.EXTSDCARD) {

                    } else {
                        if(mListener != null) return mListener.onLongClick(item);
                    }
                }
                return false;
            }
        });
    }

    public void init() {
        mList.clear();
        mAdapter.setSelectMode(false);
        mSelectCnt = 0;
        mSelectMax = 0;

        mAdapter.notifyDataSetChanged();
    }

    public void initSDCard(String[] sdcard) {
        mList.clear();
        mAdapter.setSelectMode(false);
        mSelectCnt = 0;
        mSelectMax = 0;

        mList.add(new DualListItem(sdcard[0], DualListItem.SDCARD));
        for(int i = 1; i < sdcard.length; i++) {
            mList.add(new DualListItem(sdcard[1], DualListItem.EXTSDCARD));
        }

        mAdapter.notifyDataSetChanged();
    }

    public void initFileList(String[] filelist, String up) {
        mList.clear();
        mAdapter.setSelectMode(false);
        mSelectCnt = 0;
        mSelectMax = 0;

        if(up != null) {
            mList.add(new DualListItem(up, DualListItem.UP));
        }

        if(filelist != null) {
            for(String filepath : filelist) {
                mSelectMax++;
                mList.add(new DualListItem(filepath));
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    public void add(ArrayList<String> filelist) {
        if(filelist != null) {
            for(String filepath : filelist) {
                mSelectMax++;
                mList.add(new DualListItem(filepath));
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public void rename(String source, String target) {
        for(DualListItem item : mList) {
            if(item.getPath().equals(source)) {
                item.setPath(target);
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public void setSelectMode(boolean enable) {
        mAdapter.setSelectMode(enable);
    }

    public boolean isSelectEnable() {
        return mAdapter.getSelectMode();
    }

    public void setSelectAll(boolean b) {
        for(DualListItem item : mList) {
            item.setSelect(b);
        }
        if(b) {
            mSelectCnt = mSelectMax;
        } else {
            mSelectCnt = 0;
        }
        mAdapter.notifyDataSetChanged();
    }

    public void setSelect(ArrayList<String> list) {
        for(String tmp : list) {
            for(DualListItem item : mList) {
                if(item.getPath().equals(tmp)) {
                    mSelectCnt++;
                    item.setSelect(true);
                    break;
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public boolean isSelectAll() {
        if(mSelectMax > 0 && mSelectCnt == mSelectMax) {
            return true;
        }
        return false;
    }

    public int getSelectCount() {
        return mSelectCnt;
    }

    public int getSelectMax() {
        return mSelectMax;
    }

    public ArrayList<String> getSelectList() {
        ArrayList<String> selectList = new ArrayList<String>();

        for(DualListItem item : mList) {
            if(item.getSelect()) {
                selectList.add(item.getPath());
            }
        }
        return selectList;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cWidthMeasureSpec = widthMeasureSpec;
        int cHeightMeasureSpec = heightMeasureSpec;

        ViewGroup.LayoutParams p = this.getLayoutParams();

        if(p.width == LayoutParams.MATCH_PARENT) {
            cWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(widthMeasureSpec),
                    MeasureSpec.EXACTLY);
        }
        if(p.height == LayoutParams.MATCH_PARENT) {
            cHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(heightMeasureSpec),
                    MeasureSpec.EXACTLY);
        }

        super.onMeasure(cWidthMeasureSpec, cHeightMeasureSpec);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterview, View view, int i, long l) {
        DualListItem item = (DualListItem) adapterview.getItemAtPosition(i);

        if(mAdapter.getSelectMode()) {
            if(item.getType() == DualListItem.UP ||
               item.getType() == DualListItem.SDCARD ||
               item.getType() == DualListItem.EXTSDCARD) {

            } else {
                mSelectCnt += mAdapter.doSelect(item, view);
                if(mListener != null) mListener.onClick(item);
            }
        } else {
            if(item.getType() == DualListItem.UP) {
                ApiLog.i("KhFileBrowserView : Click - UP");
                if(mListener != null) mListener.onClick(item);
            } else if(item.getType() == DualListItem.SDCARD) {
                ApiLog.i("KhFileBrowserView : Click - SDCARD");
                if(mListener != null) mListener.onClick(item);
            } else if(item.getType() == DualListItem.EXTSDCARD) {
                ApiLog.i("KhFileBrowserView : Click - EXTSDCARD");
                if(mListener != null) mListener.onClick(item);
            } else if(item.getType() == DualListItem.FOLDER) {
                ApiLog.i("KhFileBrowserView : Click - FOLDER");
                if(mListener != null) mListener.onClick(item);
            } else {
                ApiLog.i("KhFileBrowserView : Click - File");
                if(mListener != null) mListener.onClick(item);
            }
        }
    }

}
