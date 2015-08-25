package zpdl.studio.file;

import java.io.File;
import java.io.IOException;

import zpdl.studio.api.util.ApiLog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;

public class ApiFile {
    private final File mFile;
    private final ContentResolver mContentResolver;
    private Uri EXTERNAL_CONTENT_URI;

    public ApiFile(ContentResolver contentResolver, String path) {
        mContentResolver = contentResolver;
        mFile = new File(path);
        EXTERNAL_CONTENT_URI = MediaStore.Files.getContentUri("external");
    }

    public File getFile() {
        return mFile;
    }

    public boolean mkdir() throws IOException {
        if (mFile.exists()) {
            ApiLog.i("mkdir() mFile.exists()");
            return mFile.isDirectory();
        }
        ApiLog.i("mkdir() filePath = %s",mFile.getAbsoluteFile());

        ContentValues values;
        Uri uri;

        // Create a media database entry for the directory. This step will not actually cause the directory to be created.
        values = new ContentValues();
        values.put(MediaStore.Files.FileColumns.DATA, mFile.getAbsolutePath());

        uri = mContentResolver.insert(EXTERNAL_CONTENT_URI, values);

        ApiLog.i("mkdir() 1 uri = "+uri.toString());
        ApiLog.i("mkdir() 1 mFile.exists() = "+mFile.exists());

        // Create an entry for a temporary image file within the created directory.
        // This step actually causes the creation of the directory.
        values = new ContentValues();
        values.put(MediaStore.Files.FileColumns.DATA, mFile.getAbsolutePath() + "/temp.jpg");
        uri = mContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        ApiLog.i("mkdir() 2 file = "+new File(uri.getPath()).exists());
        ApiLog.i("mkdir() 2 uri = "+uri.toString());
        ApiLog.i("mkdir() 2 mFile.exists() = "+mFile.exists());
        // Delete the temporary entry.
//        mContentResolver.delete(uri, null, null);

        return mFile.exists();
    }
}
