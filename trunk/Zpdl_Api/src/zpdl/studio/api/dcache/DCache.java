package zpdl.studio.api.dcache;

import java.util.LinkedList;

import zpdl.studio.api.util.ApiLog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class DCache {
    private static final int THREAD_STATE_READY         = 0x0000;
    private static final int THREAD_STATE_RUNNING       = 0x0001;

    private LruCacheInteface        mDiskCache;
    private LruCacheInteface        mMemoryCache;

    private Task                    mTask;
    private int                     mTaskState;

    private LinkedList<DCacheParam> mList;
    private DCacheInteface          mInterface;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mInterface != null) mInterface.done((DCacheParam) msg.obj);
        }
    };

    public DCache(Context context, String uniqueName, int memoryCacheSize, int diskCacheSize, DCacheInteface reader) {
        mDiskCache   = new LruCacheDiskImpl(context, uniqueName, diskCacheSize);
        mMemoryCache = new LruCacheMemoryImpl(memoryCacheSize);

        mList = new LinkedList<DCacheParam>();
        mInterface = reader;
        mTaskState = THREAD_STATE_READY;
    }

    public void stop() {
        if(mTaskState == THREAD_STATE_RUNNING) {
            mTask.stopThread();
            mTaskState = THREAD_STATE_READY;
        }
    }

    public DCacheData load(DCacheParam param) {
        String key = param.getKey();
        DCacheData data = mMemoryCache.get(key, null);

        if(data == null) {
            synchronized (mList) {
                boolean skip = false;

                for(DCacheParam item : mList) {
                    if(item.getKey().compareTo(key) == 0) {
                        skip = true;
                        break;
                    }
                }

                if(!skip) {
                    mList.add(param);
                }
            }

            if(mTaskState == THREAD_STATE_READY) {
                mTaskState = THREAD_STATE_RUNNING;
                mTask = new Task();
                mTask.start();
            }
        } else {
            ApiLog.i("load get MemoryCache key = %s", key);
        }

        return data;
    }

    private class Task extends Thread {
        public boolean running;

        public void stopThread() {
            running = false;
        }

        @Override
        public void run() {
            running = true;

            DCacheParam param = null;
            DCacheData  data = null;

            while(running) {
                synchronized (mList) {
                    if(mList.size() > 0) {
                        param = mList.remove();
                    } else {
                        running = false;
                        mTaskState = THREAD_STATE_READY;
                        break;
                    }
                }

                String key = param.getKey();
                data = mDiskCache.get(key, mInterface);

                if(data == null) {
                    if(mInterface != null)
                        data = mInterface.load(param);
                    if(data != null) {
                        mDiskCache.put(key, data, mInterface);
                    }
                } else {
                    ApiLog.v("load get DiskCache key = "+key);
                }
                if(data != null) {
                    mMemoryCache.put(key, data, null);
                }
                param.setDCacheData(data);

                Message msg = mHandler.obtainMessage();
                msg.obj = param;
                mHandler.sendMessage(msg);
            }
        }
    }
}
