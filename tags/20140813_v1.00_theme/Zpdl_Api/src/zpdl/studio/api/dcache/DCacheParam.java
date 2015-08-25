package zpdl.studio.api.dcache;

public abstract class DCacheParam {
    private DCacheData mDCacheData;
    
    public DCacheParam() {
        mDCacheData = null;
    }

    public void setDCacheData(DCacheData d) {
        mDCacheData = d;
    }
    
    public DCacheData getDCacheData() {
        return mDCacheData;
    }

    public abstract String getKey();
}
