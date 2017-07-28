package com.novoda.downloadmanager;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.ExecutorService;

public class DownloadService extends Service {

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
        Log.e(getClass().getSimpleName(), "onCreate() " + hashCode());

        DownloadsHandler downloadsHandler = DownloadsHandler.start();
        Timer timer = new Timer(downloadsHandler);
        ContentResolver contentResolver = getContentResolver();
        DownloadObserver downloadObserver = new DownloadObserver(downloadsHandler, contentResolver);
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

    @Override
    public void onDestroy() {
        Log.e(getClass().getSimpleName(), "onDestroy() " + hashCode());
        delegate.onDestroy();
        super.onDestroy();
    }

    class Binder extends android.os.Binder {

        void setDownloadChecker(DownloadCheck downloadChecker) {
            DownloadService.this.downloadChecker = downloadChecker;
        }

        void setGlobalChecker(GlobalClientCheck globalChecker) {
            DownloadService.this.globalChecker = globalChecker;
        }

        void setServiceConnection(DownloadServiceConnection serviceConnection) {
            DownloadService.this.serviceConnection = serviceConnection;
        }

        void onBindComplete() {
            Log.e(getClass().getSimpleName(), "onBindComplete() " + hashCode());

            delegate.revertSubmittedDownloadsToQueuedDownloads();
            delegate.onServiceStart();
        }
    }

    interface ServiceStopper {

        void stopService();

    }

}