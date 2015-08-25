package zpdl.studio.duallist.view;

import java.io.File;
import java.util.List;

import zpdl.studio.api.util.ApiLog;
import zpdl.studio.file.ApiFileUtil;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class DualListItem {
    public static final int UNKNOWN         = 0;
    public static final int SUPPORT_NOT     = 1;
    public static final int SUPPORT_UNKNOW  = 2;

    public static final int UP              = 3;
    public static final int SDCARD          = 4;
    public static final int EXTSDCARD       = 5;
    public static final int FOLDER          = 6;

    public static final int TEXT_PLAIN      = 7;
    public static final int TEXT_PDF        = 8;
    public static final int TEXT_WORD       = 9;
    public static final int TEXT_EXCEL      = 10;
    public static final int TEXT_POWERPOINT = 11;
    public static final int TEXT_XML        = 12;
    public static final int TEXT_X          = 13;

    public static final int SOUND           = 14;
    public static final int MP3             = 15;
    public static final int IMAGE           = 16;
    public static final int VIDEO           = 17;

    public static final int INTERNET        = 18;
    public static final int ZIP_FILE        = 19;
    public static final int CERTIFICATE     = 20;
    public static final int CALENDAR        = 21;
    public static final int NAME_CARD       = 22;

    public static final int ANDROID_APPLICATION = 23;
    public static final int GOOGLE_EARTH    = 24;

    private String  path;
    private int     type;
    private long    size;
    private boolean select;
    private long    lastModified;

    public DualListItem(String path) {
        this(path, UNKNOWN);
    }

    public DualListItem(String path, int type) {
        this(path, type, false);
    }

    public DualListItem(String path, int type, boolean select) {
        this.path       = path;
        this.type       = type;
        this.select     = select;

        File f = new File(path);
        size = f.length();
        lastModified = f.lastModified();
    }

    public boolean isPosiibleSelect() {
        if(type == UP || type == SDCARD || type == EXTSDCARD) {
            return false;
        }
        return true;
    }

    public void setPath(String path)    { this.path = path; }
    public void setType(int type)       { this.type = type; }

    public void setSelect(boolean b) {
        if(isPosiibleSelect()) {
            this.select = b;
        }
    }

    public String       getPath()         { return path; }
    public int          getType()         { return type; }
    public boolean      getSelect()       { return select;}
    public long         getSize()         { return size; }
    public long         getLastModified() { return lastModified; }

    public String getName() {
        if(type == UP) {
            return "..";
        } else if(type == SDCARD) {
            return "SD Card";
        } else if(type == EXTSDCARD) {
            return "External SD Card";
        }

        int filenamePos = path.lastIndexOf(File.separator);
        String filename = 0 <= filenamePos ? path.substring(filenamePos + 1) : path;

        int dotPos = filename.lastIndexOf('.');
        String filenameExceptextension = 0 <= dotPos ? filename.substring(0, dotPos) : filename;

        return filenameExceptextension;
    }

    public String getExtention() {
        int filenamePos = path.lastIndexOf(File.separator);
        String filename = 0 <= filenamePos ? path.substring(filenamePos + 1) : path;

        int dotPos = filename.lastIndexOf('.');
        String extension = 0 <= dotPos ? filename.substring(dotPos + 1) : "";

        return extension;
    }

    public static String getExtention(String path) {
        int filenamePos = path.lastIndexOf(File.separator);
        String filename = 0 <= filenamePos ? path.substring(filenamePos + 1) : path;

        int dotPos = filename.lastIndexOf('.');
        String extension = 0 <= dotPos ? filename.substring(dotPos + 1) : "";

        return extension;
    }

    public int getType(PackageManager packageManager) {
        if(type != UNKNOWN) {
            return type;
        }

        if((new File(path).isDirectory())) {
            type = FOLDER;
            return FOLDER;
        }

        String mime = ApiFileUtil.getMimeType(path);

        if(mime == null) {
            type = SUPPORT_NOT;
            return type;
        }
        ApiLog.i("Mime Type = %s",mime);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndTypeAndNormalize(Uri.fromFile(new File(path)), mime);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        if(list.size() <= 0) {
            type = SUPPORT_NOT;
            return type;
        }

        int separatorIndex = mime.lastIndexOf('/');
        String top = separatorIndex < 0 ? mime : mime.substring(0, separatorIndex);
        String bottom = separatorIndex < 0 ? mime : mime.substring(separatorIndex + 1);

        if(top.startsWith("application")) {
            if(bottom.startsWith("pdf")) {
                type = TEXT_PDF;
            } else if(bottom.startsWith("msword") || bottom.startsWith("vnd.openxmlformats-officedocument.wordprocessingml")) {
                type = TEXT_WORD;
            } else if(bottom.startsWith("vnd.ms-excel") || bottom.startsWith("vnd.openxmlformats-officedocument.spreadsheetml")) {
                type = TEXT_EXCEL;
            } else if(bottom.startsWith("vnd.ms-powerpoint") || bottom.startsWith("vnd.openxmlformats-officedocument.presentationml")) {
                type = TEXT_POWERPOINT;
            } else if(bottom.startsWith("mac-compactpro")) {
                type = IMAGE;
            } else if(bottom.startsWith("ogg") || bottom.startsWith("vnd.smaf") || bottom.startsWith("vnd.stardivision.math") || bottom.startsWith("x-flac")) {
                type = SOUND;
            } else if(bottom.startsWith("x-koan")) {
                type = VIDEO;
            } else if(bottom.startsWith("x-webarchive-xml") || bottom.startsWith("xhtml+xml")) {
                type = INTERNET;
            } else if(bottom.startsWith("zip") || bottom.startsWith("x-tar") || bottom.startsWith("rar")) {
                type = ZIP_FILE;
            } else if(bottom.startsWith("vnd.google-earth")) {
                type = GOOGLE_EARTH;
            } else if(bottom.startsWith("x-pkcs12") || bottom.startsWith("x-x509")) {
                type = CERTIFICATE;
            } else if(bottom.startsWith("vnd.android.package-archive")) {
                type = ANDROID_APPLICATION;
            } else {
                type = SUPPORT_UNKNOW;
            }
        } else if(top.startsWith("text")) {
            if(bottom.startsWith("comma-separated-values")) {
                type = TEXT_EXCEL;
            } else if(bottom.startsWith("html")) {
                type = INTERNET;
            } else if(bottom.startsWith("plain") || bottom.startsWith("rtf")) {
                type = TEXT_PLAIN;
            } else if(bottom.startsWith("xml")) {
                type = TEXT_XML;
            } else if(bottom.startsWith("richtext")) {
                type = SOUND;
            } else if(bottom.startsWith("texmacs")) {
                type = VIDEO;
            } else if(bottom.startsWith("calendar") || bottom.startsWith("x-vcalendar")) {
                type = CALENDAR;
            } else if(bottom.startsWith("x-vcard")) {
                type = NAME_CARD;
            } else if(bottom.startsWith("x-")) {
                type = TEXT_X;
            } else {
                type = SUPPORT_UNKNOW;
            }
        } else if(top.startsWith("audio")) {
            if(bottom.startsWith("mpeg")) {
                type = MP3;
            } else {
                type = SOUND;
            }
        } else if(top.startsWith("image")) {
            type = IMAGE;
        } else if(top.startsWith("video")) {
            type = VIDEO;
        } else {
            type = SUPPORT_UNKNOW;
        }

        return type;
    }
}