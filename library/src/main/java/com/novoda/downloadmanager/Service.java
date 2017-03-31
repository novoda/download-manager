package com.novoda.downloadmanager;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;
import com.novoda.downloadmanager.service.Delegate;
import com.novoda.downloadmanager.service.DelegateCreator;

public class Service extends android.app.Service {

    private final Binder binder = new ServiceBinder();

    private Delegate delegate;

    private DownloadCheck downloadChecker;
    private GlobalClientCheck globalChecker;

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

    private void start() {
        Log.e("!!!", "service on start");
        delegate = createDelegate(globalChecker, downloadChecker);

        delegate.start();
    }

    private Delegate createDelegate(GlobalClientCheck globalChecker, DownloadCheck downloadChecker) {
        HandlerThread updateThread = new HandlerThread("DownloadManager-UpdateThread");
        updateThread.start();
        Handler updateHandler = new Handler(updateThread.getLooper());

        return DelegateCreator.create(
                updateThread,
                updateHandler,
                this,
                globalChecker,
                downloadChecker
        );
    }

    @Override
    public void onDestroy() {
        delegate.shutDown();
        Log.e(getClass().getSimpleName(), "Service Destroyed: " + hashCode());
        super.onDestroy();
    }

    class ServiceBinder extends Binder {

        void setDownloadChecker(DownloadCheck downloadChecker) {
            Service.this.downloadChecker = downloadChecker;
        }

        void setGlobalChecker(GlobalClientCheck globalChecker) {
            Service.this.globalChecker = globalChecker;
        }

        void start() {
            Service.this.start();
        }
    }

}
