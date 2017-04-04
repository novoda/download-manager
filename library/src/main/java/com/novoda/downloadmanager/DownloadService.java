package com.novoda.downloadmanager;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

class DownloadService extends Service {

    private final android.os.Binder binder = new Binder();

    private Delegate delegate;

    private DownloadCheck downloadChecker;
    private GlobalClientCheck globalChecker;
    private DownloadServiceConnection serviceConnection;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("!!!", "service created");
    }

    private void fooStart() {
        Log.e("!!!", "service on startService");
        delegate = createDelegate(globalChecker, downloadChecker);
        delegate.revertSubmittedDownloadsToQueuedDownloads();
        delegate.onServiceStart();
    }

    private Delegate createDelegate(GlobalClientCheck globalChecker, DownloadCheck downloadChecker) {
        HandlerThread updateThread = new HandlerThread("DownloadManager-UpdateThread");
        updateThread.start();
        Handler updateHandler = new Handler(updateThread.getLooper());

        Timer timer = new Timer(updateThread, updateHandler);
        ContentResolver contentResolver = getContentResolver();
        DownloadObserver downloadObserver = new DownloadObserver(updateHandler, contentResolver);

        return DelegateCreator.create(
                globalChecker,
                downloadChecker,
                serviceConnection,
                downloadObserver,
                timer,
                LocalBroadcastManager.getInstance(this),
                contentResolver
        );
    }

    @Override
    public void onDestroy() {
        delegate.onDestroy();
        Log.e(getClass().getSimpleName(), "Service Destroyed: " + hashCode());
        super.onDestroy();
    }

    class Binder extends android.os.Binder {

        void setDownloadChecker(DownloadCheck downloadChecker) {
            DownloadService.this.downloadChecker = downloadChecker;
        }

        void setGlobalChecker(GlobalClientCheck globalChecker) {
            DownloadService.this.globalChecker = globalChecker;
        }

        void fooStart() {
            DownloadService.this.fooStart();
        }

        void setServiceConnection(DownloadServiceConnection serviceConnection) {
            Log.d("!!!", "setServiceConnection():");
            DownloadService.this.serviceConnection = serviceConnection;
        }
    }

}
