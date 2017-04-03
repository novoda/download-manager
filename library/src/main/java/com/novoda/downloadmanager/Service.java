package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;
import com.novoda.downloadmanager.service.Delegate;
import com.novoda.downloadmanager.service.DelegateCreator;
import com.novoda.downloadmanager.service.DownloadObserver;
import com.novoda.downloadmanager.service.Timer;

public class Service extends android.app.Service {

    private final Binder binder = new DownloadServiceBinder();

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

    class DownloadServiceBinder extends Binder {

        void setDownloadChecker(DownloadCheck downloadChecker) {
            Service.this.downloadChecker = downloadChecker;
        }

        void setGlobalChecker(GlobalClientCheck globalChecker) {
            Service.this.globalChecker = globalChecker;
        }

        void fooStart() {
            Service.this.fooStart();
        }

        void setServiceConnection(DownloadServiceConnection serviceConnection) {
            Log.d("!!!", "setServiceConnection():");
            Service.this.serviceConnection = serviceConnection;
        }
    }

}
