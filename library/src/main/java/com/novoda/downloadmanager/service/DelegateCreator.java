package com.novoda.downloadmanager.service;

import android.content.ContentResolver;
import android.support.v4.content.LocalBroadcastManager;

import com.novoda.downloadmanager.DownloadServiceConnection;
import com.novoda.downloadmanager.Pauser;
import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;
import com.novoda.downloadmanager.download.ContentLengthFetcher;
import com.novoda.downloadmanager.download.DownloadDatabaseWrapper;
import com.novoda.downloadmanager.download.DownloadDatabaseWrapperCreator;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.ExecutorService;

public class DelegateCreator {

    public static Delegate create(GlobalClientCheck globalClientCheck,
                                  DownloadCheck downloadCheck,
                                  DownloadServiceConnection downloadServiceConnection,
                                  DownloadObserver downloadObserver,
                                  Timer timer,
                                  LocalBroadcastManager localBroadcastManager,
                                  ContentResolver contentResolver) {

        DownloadDatabaseWrapper downloadDatabaseWrapper = DownloadDatabaseWrapperCreator.create(contentResolver);
        Pauser pauser = new Pauser(localBroadcastManager);
        DownloadExecutorFactory factory = new DownloadExecutorFactory();
        ExecutorService executor = factory.createExecutor();
        ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher(new OkHttpClient());
        TotalFileSizeUpdater totalFileSizeUpdater = new TotalFileSizeUpdater(downloadDatabaseWrapper, contentLengthFetcher);
        DownloadTaskSubmitter downloadTaskSubmitter = new DownloadTaskSubmitter(downloadDatabaseWrapper, executor, pauser, downloadCheck);

        return new Delegate(downloadObserver, downloadTaskSubmitter, timer, globalClientCheck, downloadDatabaseWrapper, downloadServiceConnection, totalFileSizeUpdater);
    }

}
