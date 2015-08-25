package zpdl.studio.api.util;

import java.io.IOException;

import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

public class ApiMediaScanner {
    MediaScannerConnection mediaScanner;
    ContentResolver contentResolver;
    Object mediaScannerConnectSyncObject = new Object();

    public ApiMediaScanner(Context context) {
        mediaScanner = new MediaScannerConnection (context, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
            }

            @Override
            public void onMediaScannerConnected() {
                synchronized (mediaScannerConnectSyncObject) {
                    mediaScannerConnectSyncObject.notifyAll();
                }
            }
        });
        contentResolver = context.getContentResolver();
    }

    public void release() {
        _disconnet();
        mediaScanner = null;
        contentResolver = null;
    }

    public void insertFile(String path) throws IOException {
        _connet();
        mediaScanner.scanFile(path, null);
    }

    public void deleteFile(String path) {
        contentResolver.delete(MediaStore.Files.getContentUri("external"),
                MediaStore.Files.FileColumns.DATA + "=?",
                new String[] { path });
    }

    public void moveFile(String in, String out) throws IOException {
        _connet();
        mediaScanner.scanFile(out, null);

        contentResolver.delete(MediaStore.Files.getContentUri("external"),
                MediaStore.Files.FileColumns.DATA + "=?",
                new String[] { in });
    }

    public static void insertfolder(Context context, String path) {
        MediaScannerConnection.scanFile(context, new String[] { path }, null, null);
    }

    public static void renameFile(Context context, String in, String out) {
        context.getContentResolver().delete(MediaStore.Files.getContentUri("external"),
                MediaStore.Files.FileColumns.DATA + "=?",
                new String[] { in });

        MediaScannerConnection.scanFile(context, new String[] { out }, null, null);
    }

    private void _connet() throws IOException {
        if(!mediaScanner.isConnected()) {
            mediaScanner.connect();
            synchronized (mediaScannerConnectSyncObject) {
                try {
                    mediaScannerConnectSyncObject.wait(3000);
                    if(!mediaScanner.isConnected()) {
                        throw new IOException("Copy failed : mediaScanner is connect time out");
                    }
                } catch(InterruptedException e) {
                    throw new IOException(e);
                }
            }
        }
    }

    private void _disconnet() {
        if(mediaScanner.isConnected()) {
            mediaScanner.disconnect();
        }
    }
}
