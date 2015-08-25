package zpdl.studio.file;

import android.webkit.MimeTypeMap;

public class ApiFileUtil {
    public static String getMimeType(String url)
    {
        String type = null;

        int filenamePos = url.lastIndexOf('/');
        String filename = 0 <= filenamePos ? url.substring(filenamePos + 1) : url;

        int dotPos = filename.lastIndexOf('.');
        String extension = 0 <= dotPos ? filename.substring(dotPos + 1).toLowerCase() : filename.toLowerCase();

        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
            if(type == null && extension.equals("mkv")) {
                type = "video/x-matroska";
            }
        }
        return type;
    }
}
