package zpdl.studio.duallist;

import zpdl.studio.duallist.view.DualListItem;
import zpdl.studio.duallist.view.DualListLayout;
import zpdl.studio.duallist.view.DualListLayout.onDualListLayoutListener;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class DualListFragment extends Fragment {
    private static final String KEY_PATH = "KhPath";

    private DualListLayout mKhDualListLayout;
    private onDualListFragmentListener mListener;

    public interface onDualListFragmentListener {
        void    onFolderChange(String path);
        void    onFileClick(String path);
        boolean onLongClick(DualListItem item);
        void    onUpdateMenu();
    }

    public static DualListFragment newInstance(String path) {
        DualListFragment f = new DualListFragment();

        Bundle args = new Bundle();
        args.putString(KEY_PATH, path);
        f.setArguments(args);

        return f;
    }

    public void setRegisterForContextMenu(Activity a) {
        a.registerForContextMenu(mKhDualListLayout.getForderView());
        a.registerForContextMenu(mKhDualListLayout.getFileView());
    }

    public void setPath(String path, boolean center) {
        mKhDualListLayout.init(path, center);
    }

    public String getPath() {
        return mKhDualListLayout.getPath();
    }

    public boolean isSelectMode() {
        return mKhDualListLayout.isSelectMode();
    }

    public int getSelectedCount() {
        return mKhDualListLayout.getFileSelectCount();
    }

    public boolean isSelectPossible() {
        return mKhDualListLayout.getFileSelectMax() > 0;
    }

    public boolean isSelectAll() {
        return mKhDualListLayout.isFileSelectAll();
    }

    public void selectAll(boolean all) {
        mKhDualListLayout.setFileSelectAll(all);
    }

    public String[] getFileSelectArray() {
        return mKhDualListLayout.getFileSelectArray();
    }

    public void modeBase() {
        mKhDualListLayout.modeBase();
    }

    public void modeSelect() {
        mKhDualListLayout.modeSelect();
    }

    public void addFolder(String folder) {
        mKhDualListLayout.addFolder(folder);
    }

    public void rename(String source, String target) {
        mKhDualListLayout.rename(source, target);
    }

    public boolean back() {
        return mKhDualListLayout.modeBack();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (onDualListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onDualListFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mKhDualListLayout = new DualListLayout(getActivity());
        mKhDualListLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mKhDualListLayout.setOnDualListLayoutListener(new onDualListLayoutListener(){
            @Override
            public void onFolderChange(String path) {
                if(mListener != null) mListener.onFolderChange(path);
            }

            @Override
            public void onFileClick(String path) {
                if(mListener != null) mListener.onFileClick(path);
            }

            @Override
            public void onUpdateMenu() {
                if(mListener != null) mListener.onUpdateMenu();
            }

            @Override
            public boolean onLongClick(DualListItem item) {
                if(mListener != null) return mListener.onLongClick(item);
                return false;
            }
        });

        return mKhDualListLayout;
    }
}
