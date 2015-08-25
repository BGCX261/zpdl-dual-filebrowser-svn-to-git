package zpdl.studio.file;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ApiFileService extends Service {
    private final IBinder mBinder = new ApiFileBinder();

    private ApiFileServiceListener mListener;
    private int                    mNotificationIcon;

    private boolean                        mExecutorRunning;
    private Object                         mExecutorSync;
    private ArrayList<ApiFileServiceParam> mExecutorData;
    private ExecutorService                mExecutorService;

    private class ApiFileServiceParam {
        public boolean  isRunning;

        public ApiFileServiceParam() {
            isRunning = true;
        }
    }

    public interface ApiFileServiceListener {
        void onFileInit(int count, String name);
        void onFileProgress(int count, int progress, String name);
        void onFileComplete(boolean err, String result);
    }

    public void setListener(ApiFileServiceListener l) {
        synchronized (mExecutorSync) {
            mListener = l;
        }
    }

    public void setNotificationIcon(int res) {
        mNotificationIcon = res;
    }

    public void copy(String[] in, String out) {
        mExecutorData.add(new ApiFileServiceParam());
        mExecutorService.execute(new FileRunnable());
    }

    public static void bindService(Context c, ServiceConnection connection) {
        boolean isStartService = false;
        ActivityManager manager = (ActivityManager) c.getSystemService(Activity.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ApiFileService.class.getName().equals(service.service.getClassName())) {
                isStartService = true;
                break;
            }
        }

        if(!isStartService) {
            Intent intent = new Intent(c, ApiFileService.class);
            c.startService(intent);
        }

        Intent intent = new Intent(c, ApiFileService.class);
        c.bindService(intent, connection, 0);
    }

    public static void unbindService(Context c, ServiceConnection connection) {
        c.unbindService(connection);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mListener = null;
        mNotificationIcon = 0;

        mExecutorRunning = false;
        mExecutorSync = new Object();
        mExecutorData = new ArrayList<ApiFileService.ApiFileServiceParam>();
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onDestroy() {
        Log.e("KKH","onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("KKH","onUnbind");
        return super.onUnbind(intent);
    }

    public class ApiFileBinder extends Binder {
        public ApiFileService getService() {
            return  ApiFileService.this;
        }
    }

    private class FileRunnable implements Runnable {
        @Override
        public void run() {
            NotificationManager  notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(ApiFileService.this);

            while(mExecutorData.size() > 0) {
                mExecutorRunning = true;
                ApiFileServiceParam param = mExecutorData.remove(0);
                if(!param.isRunning) {
                    continue;
                }

                builder.setContentTitle("Picture Download")
                       .setContentText("Download in progress")
                       .setProgress(0, 0, true)
                       .setOngoing(true);

                Notification notification = builder.build();
                notification.flags |= Notification.FLAG_NO_CLEAR;
                startForeground(1234, notification);

                int i = 0;
                while(i < 100) {
                    Log.i("KKH","startTask i = "+i);
                    i += 1;

                    builder.setProgress(100, i, false);
                    notifyManager.notify(1234, builder.build());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
//                    if(mListener != null) mListener.call(i);
                }

                builder.setContentText("Download complete")
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                notifyManager.notify(0, builder.build());

                stopForeground(true);
            }
            stopSelf();
        }
    }

    public void startTask() {
        mExecutorData.add(new ApiFileServiceParam());
        mExecutorService.execute(new FileRunnable());
    }

    private void _task() {
        int i = 0;
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification.Builder mBuilder = new Notification.Builder(ApiFileService.this);
        mBuilder.setContentTitle("Picture Download")
                .setContentText("Download in progress")
//                .setSmallIcon(R.drawable.ic_launcher)
//                .set
                    ;

        mBuilder.setProgress(0, 0, true);
        mBuilder.setOngoing(true);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(1234, notification);

        while(i < 100) {
            Log.i("KKH","startTask i = "+i);
            i += 1;

            mBuilder.setProgress(100, i, false);

            mNotifyManager.notify(1234, mBuilder.build());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
//            if(mListener != null) mListener.call(i);
        }

        mBuilder.setContentText("Download complete")
                .setProgress(0, 0, false)
                .setOngoing(false);
        mNotifyManager.notify(0, mBuilder.build());

        stopForeground(true);
    }
}
