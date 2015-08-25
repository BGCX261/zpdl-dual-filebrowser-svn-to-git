package zpdl.studio.api.dcache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import zpdl.studio.api.util.ApiLog;
import android.content.Context;
import android.os.Environment;

public class LruCacheDiskImpl implements LruCacheInteface {
    private DiskLruCache mDiskCache;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;

    public LruCacheDiskImpl( Context context,String uniqueName, int diskCacheSize) {
        try {
            final File diskCacheDir = getDiskCacheDir(context, uniqueName );
            mDiskCache = DiskLruCache.open( diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean writeBitmapToFile(DCacheData data, DiskLruCache.Editor editor, DCacheInteface writer )
        throws IOException, FileNotFoundException {
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream( 0 ), Util.IO_BUFFER_SIZE );
            if(writer != null) writer.write(data, out);
        } finally {
            if(out != null) {
                out.close();
            }
        }
        return true;
    }

    private File getDiskCacheDir(Context context, String uniqueName) {

    // Check if media is mounted or storage is built-in, if so, try and use external cache dir
    // otherwise use internal cache dir
        final String cachePath =
            Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                    !Util.isExternalStorageRemovable() ?
                    Util.getExternalCacheDir(context).getPath() :
                    context.getCacheDir().getPath();
//        KVideoCapture application = ((KVideoCapture) context.getApplicationContext());
//        final String cachePath = application.getStoragePath() + File.separator +String.valueOf("cache");

        return new File(cachePath + File.separator + uniqueName);
    }

    @Override
    public void put(String key, DCacheData data, DCacheInteface writer) {
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit( key );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( data, editor, writer ) ) {
                mDiskCache.flush();
                editor.commit();
                ApiLog.v("KhBitmapLruCacheDisk : image put on disk cache " + key);
            } else {
                editor.abort();
                ApiLog.w("KhBitmapLruCacheDisk : ERROR 1 on: image put on disk cache " + key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        	ApiLog.w("KhBitmapLruCacheDisk : ERROR 2 on: image put on disk cache " + key);
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }

    }

    @Override
    public DCacheData get(String key, DCacheInteface reader) {
        DiskLruCache.Snapshot snapshot = null;
        BufferedInputStream buffIn = null;
        DCacheData data = null;
        try {
            snapshot = mDiskCache.get(key);
            if ( snapshot == null ) {
            	return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );

            if ( in != null ) {
                buffIn = new BufferedInputStream( in, Util.IO_BUFFER_SIZE );
                if(reader != null)
                    data = reader.read(buffIn);
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if(buffIn != null) {
                try {
                    buffIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if ( snapshot != null ) {
                snapshot.close();
            }
        }
        return data;
    }

    @Override
    public void remove(String key) {
        try {
            mDiskCache.remove(key);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        try {
            mDiskCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

}
