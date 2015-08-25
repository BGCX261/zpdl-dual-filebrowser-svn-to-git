package zpdl.studio.api.dcache;

import android.util.LruCache;

public class LruCacheMemoryImpl implements LruCacheInteface {

    private LruCache<String, DCacheData> lruCache;

    public LruCacheMemoryImpl(int maxMemory) {
        lruCache = new LruCache<String, DCacheData>(maxMemory) {
            @Override
            protected int sizeOf(String key, DCacheData data) {
                    return data.size();
                }
            @Override
            protected void entryRemoved(boolean evicted, String key, DCacheData oldValue, DCacheData newValue) {
                oldValue.recycle();
            };
        };
    }

    @Override
    public void put(String key, DCacheData data, DCacheInteface writer) {
        if (data == null)
            return;
        lruCache.put(key, data);
    }

    @Override
    public DCacheData get(String key, DCacheInteface reader) {
        return lruCache.get(key);
    }

    @Override
    public void remove(String key) {
        lruCache.remove(key);
    }

    @Override
    public void clear() {
        lruCache.evictAll();
    }
}
