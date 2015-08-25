package zpdl.studio.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import zpdl.studio.api.util.ApiMediaScanner;
import android.content.Context;

public class _ApiFile {
    public static final int READY  = 0x0000;
    public static final int COPY   = 0x0001;
    public static final int MOVE   = 0x0002;
    public static final int DELETE = 0x0003;
    public static final int RENAME = 0x0004;
    public static final int CREATEFOLDER = 0x0005;

    private static int mode = READY;
    private static Object mSyncObject = new Object();
    private static onFileListener listener = null;

    public interface onFileListener {
        void onFileInit(int count, String name);
        void onFileProgress(int count, int progress, String name);
        void onFileComplete(boolean err, String result);
    }

    public static void setOnFileListener(onFileListener l) {
        synchronized (mSyncObject) {
            listener = l;
        }
    }

    public static int getMode() {
        synchronized (mSyncObject) {
            return mode;
        }
    }

    public static void cancel() {
        synchronized (mSyncObject) {
            mode = READY;
        }
    }

    public static void copy(final Context context, final String[] in, final String out) {
        synchronized (mSyncObject) {
            mode = COPY;
        }

        new Thread(new Runnable() {
            long progressMaxSize;
            int  progressMaxCnt;
            long progressSize;
            int  progressCnt;
            int  progressValue;
            String error;

            ApiMediaScanner mMediaScanner;

            @Override
            public void run() {
                try {
                    mMediaScanner = new ApiMediaScanner(context);

                    error = null;
                    ApiFileSizeParam sc = _getFileSizeAndCount(in);
                    progressMaxSize = sc.getSize();
                    progressMaxCnt = sc.getCount();
                    progressSize = 0;
                    progressCnt = 0;
                    progressValue = 0;

                    synchronized (mSyncObject) {
                        if(listener != null) listener.onFileInit(progressMaxCnt, in[0]);
                    }

                    for(String path : in) {
                        File inFile = new File(path);
                        File outFile = _createOutputFile(mode, out + File.separator + inFile.getName());
                        _copy(inFile, outFile);

                        mMediaScanner.insertFile(outFile.getPath());

                        synchronized (mSyncObject) {
                            if(mode != COPY) {
                                if(outFile.exists() && outFile.isFile()) {
                                    outFile.delete();
                                }
                                break;
                            }
                        }
                    }
                }
                catch (IOException e) {
                    error = e.getMessage();
                    e.printStackTrace();
                } finally {
                    if(mMediaScanner != null) {
                        mMediaScanner.release();
                        mMediaScanner = null;
                    }

                    synchronized (mSyncObject) {
                        if(error != null) {
                            if(listener != null) listener.onFileComplete(true, error);
                        } else {
                            if(listener != null) listener.onFileComplete(false, out);
                        }
                        mode = READY;
                    }
                }
            }

            private void _copy(File in, File out) throws IOException {
                if(in.exists() && in.isDirectory()) {
                    out.mkdir();
                    for(File cFile : in.listFiles()) {
                        _copy(cFile, new File(out, cFile.getName()));
                        synchronized (mSyncObject) {
                            if(mode != COPY)
                                return;
                        }
                    }
                } else if(in.exists() && in.isFile()) {
                    FileInputStream inputStream = null;
                    FileOutputStream outputStream = null;
                    BufferedInputStream bin = null;
                    BufferedOutputStream bout = null;

                    try {
                        inputStream = new FileInputStream(in);
                        outputStream = new FileOutputStream(out);
                        bin = new BufferedInputStream(inputStream);
                        bout = new BufferedOutputStream(outputStream);

                        progressCnt ++;
                        int bytesRead = 0;
                        byte[] buffer = new byte[1024];
                        while ((bytesRead = bin.read(buffer, 0, 1024)) != -1) {
                            bout.write(buffer, 0, bytesRead);
                            progressSize += bytesRead;
                            int newprogress = (int)((double) progressSize * 100 / (double) progressMaxSize);

                            if(newprogress > progressValue && progressValue <= 100) {
                                progressValue = newprogress;
                                synchronized (mSyncObject) {
                                    if(listener != null) listener.onFileProgress(progressCnt, progressValue, in.getName());
                                    if(mode != COPY) break;
                                }
                            }
                        }
                    } finally {
                        bout.close();
                        bin.close();
                        outputStream.close();
                        inputStream.close();
                    }
                }
            }
        }).start();
    }

    public static void move(final Context context, final String[] in, final String out) {
        synchronized (mSyncObject) {
            mode = MOVE;
        }

        new Thread(new Runnable() {
            long progressMaxSize;
            int  progressMaxCnt;
            long progressSize;
            int  progressCnt;
            int  progressValue;
            String error;

            ApiMediaScanner mMediaScanner;

            @Override
            public void run() {
                try {
                    mMediaScanner = new ApiMediaScanner(context);

                    error = null;
                    ApiFileSizeParam sc = _getFileSizeAndCount(in);
                    progressMaxSize = sc.getSize();
                    progressMaxCnt = sc.getCount();
                    progressSize = 0;
                    progressCnt = 0;
                    progressValue = 0;

                    synchronized (mSyncObject) {
                        if(listener != null) listener.onFileInit(progressMaxCnt, in[0]);
                    }

                    for(String path : in) {
                        File inFile = new File(path);
                        File outFile = _createOutputFile(mode, out + File.separator + inFile.getName());
                        _move(inFile, outFile);

                        mMediaScanner.moveFile(inFile.getPath(), outFile.getPath());

                        synchronized (mSyncObject) {
                            if(mode != MOVE) {
                                break;
                            }
                        }
                    }
                }
                catch (IOException e) {
                    error = e.getMessage();
                    e.printStackTrace();
                } finally {
                    if(mMediaScanner != null) {
                        mMediaScanner.release();
                        mMediaScanner = null;
                    }
                    synchronized (mSyncObject) {
                        if(error != null) {
                            if(listener != null) listener.onFileComplete(true, error);
                        } else {
                            if(listener != null) listener.onFileComplete(false, out);
                        }
                        mode = READY;
                    }
                }
            }

            private void _move(File in, File out) throws IOException {
                if(in.renameTo(out)) {
                    progressCnt++;
                    progressSize += in.length();
                    progressValue = (int)((double) progressSize * 100 / (double) progressMaxSize);
                    synchronized (mSyncObject) {
                        if(listener != null) listener.onFileProgress(progressCnt, progressValue, in.getName());
                    }
                } else {
                    _copy(in, out);
                }
            }

            private void _copy(File in, File out) throws IOException {
                if(in.exists() && in.isDirectory()) {
                    out.mkdir();
                    for(File cFile : in.listFiles()) {
                        _copy(cFile, new File(out, cFile.getName()));
                        synchronized (mSyncObject) {
                            if(mode != COPY)
                                return;
                        }
                    }
                    in.delete();
                } else if(in.exists() && in.isFile()) {
                    FileInputStream inputStream = null;
                    FileOutputStream outputStream = null;
                    BufferedInputStream bin = null;
                    BufferedOutputStream bout = null;

                    try {
                        inputStream = new FileInputStream(in);
                        outputStream = new FileOutputStream(out);
                        bin = new BufferedInputStream(inputStream);
                        bout = new BufferedOutputStream(outputStream);

                        progressCnt ++;
                        int bytesRead = 0;
                        byte[] buffer = new byte[1024];
                        while ((bytesRead = bin.read(buffer, 0, 1024)) != -1 && mode != COPY) {
                            bout.write(buffer, 0, bytesRead);
                            progressSize += bytesRead;
                            int newprogress = (int)((double) progressSize * 100 / (double) progressMaxSize);
                            if(newprogress > progressValue && progressValue <= 100) {
                                progressValue = newprogress;
                                synchronized (mSyncObject) {
                                    if(listener != null) listener.onFileProgress(progressCnt, progressValue, in.getName());
                                }
                            }
                        }
                    } finally {
                        bout.close();
                        bin.close();
                        outputStream.close();
                        inputStream.close();

                        in.delete();
                    }
                }
            }
        }).start();
    }

    public static void delete(final Context context, final String[] in) {
        synchronized (mSyncObject) {
            mode = DELETE;
        }

        new Thread(new Runnable() {
            long progressMaxSize;
            int  progressMaxCnt;
            long progressSize;
            int  progressCnt;
            int  progressValue;
            String error;

            ApiMediaScanner mMediaScanner;

            @Override
            public void run() {
                try {
                    mMediaScanner = new ApiMediaScanner(context);

                    error = null;

                    ApiFileSizeParam sc = _getFileSizeAndCount(in);
                    progressMaxSize = sc.getSize();
                    progressMaxCnt = sc.getCount();
                    progressSize = 0;
                    progressCnt = 0;
                    progressValue = 0;

                    synchronized (mSyncObject) {
                        if(listener != null) listener.onFileInit(progressMaxCnt, in[0]);
                    }

                    for(String path : in) {
                        synchronized (mSyncObject) {
                            if(mode != DELETE)
                                break;
                        }
                        File inFile = new File(path);
                        _delete(inFile);

                        mMediaScanner.deleteFile(inFile.getPath());
                    }
                }
                catch (IOException e) {
                    error = e.getMessage();
                    e.printStackTrace();
                } finally {
                    if(mMediaScanner != null) {
                        mMediaScanner.release();
                        mMediaScanner = null;
                    }

                    synchronized (mSyncObject) {
                        if(error != null) {
                            if(listener != null) listener.onFileComplete(true, error);
                        } else {
                            if(listener != null) listener.onFileComplete(false, null);
                        }
                        mode = READY;
                    }
                }
            }

            private void _delete(File f) throws IOException {
                if(f.exists() && f.isDirectory()) {
                    for(File dFile : f.listFiles()) {
                        _delete(dFile);
                        synchronized (mSyncObject) {
                            if(mode != DELETE)
                                return;
                        }
                    }

                    if(!f.delete()) {
                        throw new IOException("Failed delete folder : " + f.getPath());
                    }
                } else if(f.exists() && f.isFile()) {
                    progressSize += f.length();
                    progressCnt ++;
                    if(f.delete()) {
                        progressValue = (int)((double) progressSize * 100 / (double) progressMaxSize);
                        synchronized (mSyncObject) {
                            if(listener != null) listener.onFileProgress(progressCnt, progressValue, f.getName());
                        }
                    } else {
                        throw new IOException("Failed delete file : " + f.getPath());
                    }
                }
            }
        }).start();
    }

    public static String rename(Context context, String in, String out) throws IOException {
        File inFile = new File(in);
        File outFile = _createOutputFile(RENAME, out);

        if(inFile.exists() && !outFile.exists()) {
            if(inFile.renameTo(outFile)) {
                ApiMediaScanner.renameFile(context, inFile.getPath(), outFile.getPath());

                return outFile.getPath();
            }
        }
        throw new IOException("Failed Rename : in = " + inFile.getPath() + " out = " + outFile.getPath());
    }

    public static String createfolder(Context context, String src) throws IOException {
        File f = _createOutputFile(CREATEFOLDER, src);

        if(f.mkdir() && f.isDirectory()) {
            ApiMediaScanner.insertfolder(context, f.getPath());

            return f.getPath();
        } else {
            throw new IOException("Failed create folder : " + f.getPath());
        }
    }

    public static long getFileCount(String[] fileList) {
        long count = 0;

        for(String path : fileList) {
            count += getFileCount(path);
        }
        return count;
    }

    public static long getFileCount(String path) {
        long count = 0;

        LinkedList<String> ll = new LinkedList<String>();
        ll.offer(path);

        while(!ll.isEmpty()) {
            File file = new File(ll.poll());
            if(file.exists() && file.isDirectory()) {
                for(File addFile : file.listFiles()) {
                    ll.offer(addFile.getPath());
                }
            } else if(file.exists() && file.isFile()) {
                count++;
            }
        }

        return count;
    }

    public static long getFileSize(String[] fileList) {
        long size = 0;

        for(String path : fileList) {
            size += getFileSize(path);
        }
        return size;
    }

    public static long getFileSize(String path) {
        long size = 0;

        LinkedList<String> ll = new LinkedList<String>();
        ll.offer(path);

        while(!ll.isEmpty()) {
            File file = new File(ll.poll());

            if(file.exists() && file.isDirectory()) {
                for(File addFile : file.listFiles()) {
                    ll.offer(addFile.getPath());
                }
            } else if(file.exists() && file.isFile()) {
                size += file.length();
            }
        }

        return size;
    }

    private static File _createOutputFile(int mode, String in) {
        File outFile = new File(in);

        while(outFile.exists()) {
            String path = outFile.getPath();

            int index = path.lastIndexOf(String.valueOf('.'));
            String extention = null;
            String outname = null;
            if(index > 0) {
                outname = path.substring(0, index);
                extention = path.substring(index);
            } else {
                outname = path;
            }

            if(mode == COPY) {
                outFile = new File(outname + "_copy" + (extention == null ? "" : extention));
            } else if(mode == MOVE) {
                outFile = new File(outname + "_move" + (extention == null ? "" : extention));
            } else if(mode == RENAME) {
                outFile = new File(outname + "_rename" + (extention == null ? "" : extention));
            } else if(mode == CREATEFOLDER) {
                outFile = new File(outname + "_folder" + (extention == null ? "" : extention));
            } else {
                return null;
            }
        }

        return outFile;
    }

    private static ApiFileSizeParam _getFileSizeAndCount(String[] fileList) {
        ApiFileSizeParam sizeAndcount = new ApiFileSizeParam();
        for(String path : fileList) {
            sizeAndcount.plus(_getFileSizeAndCount(path));
        }
        return sizeAndcount;
    }

    private static ApiFileSizeParam _getFileSizeAndCount(String path) {
        ApiFileSizeParam sizeAndcount = new ApiFileSizeParam();
        LinkedList<String> ll = new LinkedList<String>();
        ll.offer(path);

        while(!ll.isEmpty()) {
            File file = new File(ll.poll());

            if(file.exists() && file.isDirectory()) {
                for(File addFile : file.listFiles()) {
                    ll.offer(addFile.getPath());
                }
            } else if(file.exists() && file.isFile()) {
                sizeAndcount.plusSize(file.length());
                sizeAndcount.plusCount(1);
            }
        }
        return sizeAndcount;
    }
}
