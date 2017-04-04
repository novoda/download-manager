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

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.ExecutorService;

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
        HandlerThread updateThread = new HandlerThread("DownloadManager-UpdateThread");
        updateThread.start();
        Handler updateHandler = new Handler(updateThread.getLooper());

        Timer timer = new Timer(updateThread, updateHandler);
        ContentResolver contentResolver = getContentResolver();
        DownloadObserver downloadObserver = new DownloadObserver(updateHandler, contentResolver);

        DownloadDatabaseWrapper downloadDatabaseWrapper = DownloadDatabaseWrapperCreator.create(contentResolver);
        Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(this));
        DownloadExecutorFactory factory = new DownloadExecutorFactory();
        ExecutorService executor = factory.createExecutor();
        ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher(new OkHttpClient());
        TotalFileSizeUpdater totalFileSizeUpdater = new TotalFileSizeUpdater(downloadDatabaseWrapper, contentLengthFetcher);
        DownloadTaskSubmitter downloadTaskSubmitter = new DownloadTaskSubmitter(downloadDatabaseWrapper, executor, pauser, lazyDownloadCheck);

        delegate = new Delegate(downloadObserver, downloadTaskSubmitter, timer, lazyGlobalClientCheck, downloadDatabaseWrapper, lazyServiceConnectionStopper, totalFileSizeUpdater);
    }

    private final GlobalClientCheck lazyGlobalClientCheck = new GlobalClientCheck() {
        @Override
        public ClientCheckResult onGlobalCheck() {
            return globalChecker.onGlobalCheck();
        }
    };

    private final DownloadCheck lazyDownloadCheck = new DownloadCheck() {
        @Override
        public ClientCheckResult isAllowedToDownload(Download download) {
            return downloadChecker.isAllowedToDownload(download);
        }
    };

    private final ServiceStopper lazyServiceConnectionStopper = new ServiceStopper() {
        @Override
        public void stopService() {
            serviceConnection.stopService();
        }
    };

    private void fooStart() {
        Log.e("!!!", "service on startService");
        delegate.revertSubmittedDownloadsToQueuedDownloads();
        delegate.onServiceStart();
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

    interface ServiceStopper {

        void stopService();

    }

}
