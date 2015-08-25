package zpdl.studio.duallist.util;

import java.io.File;

import zpdl.studio.api.dcache.DCacheParam;
import zpdl.studio.duallist.view.DualListItem;
import zpdl.studio.duallist.view.DualListRowView;

public class DualListDCacheParam extends DCacheParam {
    private String                  mKey;
    private int                     mType;
    private String                  mPath;
    private DualListRowView      mRawView;

    public DualListDCacheParam(DualListItem item, DualListRowView view) {
        super();

        long size = item.getSize();
        long lastModified = item.getLastModified();

        mType = item.getType();
        mPath = item.getPath();

        mRawView = view;

        StringBuilder sb = new StringBuilder();

        sb.append(lastModified);
        sb.append(size);

        int separatorStartIndex = 0;
        int separatorEndIndex = mPath.indexOf(File.separator);
        while(separatorEndIndex >= 0) {
            String key = mPath.substring(separatorStartIndex, separatorEndIndex);
            if(key.length() > 0) {
                byte[] bKey = key.getBytes();
                int iKey = 0;
                for(int i = 0; i < bKey.length; i++) {
                    iKey += bKey[i];
                }
                sb.append(iKey);
            }
            separatorStartIndex = separatorEndIndex + 1;
            separatorEndIndex = mPath.indexOf(File.separator, separatorEndIndex + 1);
        }
        sb.append(mPath.substring(separatorStartIndex, mPath.length()));

        mKey = sb.toString();
    }

    public int getType() {
        return mType;
    }

    public String getPath() {
        return mPath;
    }

    public DualListRowView getView() {
        return mRawView;
    }

    @Override
    public String getKey() {
        return mKey;
    }
}
