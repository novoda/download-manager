package com.novoda.downloadmanager.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;

import com.novoda.downloadmanager.DownloadServiceConnection;
import com.novoda.downloadmanager.Pauser;
import com.novoda.downloadmanager.Service;
import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;
import com.novoda.downloadmanager.download.ContentLengthFetcher;
import com.novoda.downloadmanager.download.DownloadDatabaseWrapper;
import com.novoda.downloadmanager.download.DownloadHandlerCreator;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.ExecutorService;

public class DelegateCreator {

    public static Delegate create(HandlerThread updateThread,
                                  Handler updateHandler,
                                  Service service,
                                  GlobalClientCheck globalClientCheck,
                                  DownloadCheck downloadCheck,
                                  DownloadServiceConnection downloadServiceConnection) {
        UpdateScheduler updateScheduler = new UpdateScheduler(updateThread, updateHandler);

        DownloadDatabaseWrapper downloadDatabaseWrapper = DownloadHandlerCreator.create(service.getContentResolver());
        Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(service));
        DownloadExecutorFactory factory = new DownloadExecutorFactory();
        ExecutorService executor = factory.createExecutor();
        ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher(new OkHttpClient());
        TotalFileSizeUpdater totalFileSizeUpdater = new TotalFileSizeUpdater(downloadDatabaseWrapper, contentLengthFetcher);
        DownloadUpdater downloadUpdater = new DownloadUpdater(downloadDatabaseWrapper, executor, pauser, downloadCheck);

        DownloadObserver downloadObserver = new DownloadObserver(updateHandler, service.getContentResolver());
        return new Delegate(downloadObserver, downloadUpdater, service, updateScheduler, globalClientCheck, downloadDatabaseWrapper, downloadServiceConnection, totalFileSizeUpdater);
    }

}
