package zpdl.studio.api.dcache;

public interface LruCacheInteface {
    public void             put(String key, DCacheData data, DCacheInteface writer);

    public DCacheData       get(String key, DCacheInteface reader);

    public void             remove(String key);

    public void             clear();
}
