package com.novoda.downloadmanager.service;

import android.app.Service;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;

import com.novoda.downloadmanager.Pauser;
import com.novoda.downloadmanager.download.DownloadHandler;
import com.novoda.downloadmanager.download.DownloadHandlerCreator;

import java.util.concurrent.ExecutorService;

public class DelegateCreator {

    public static Delegate create(HandlerThread updateThread, Handler updateHandler, Service service) {
        UpdateScheduler updateScheduler = new UpdateScheduler(updateThread, updateHandler);

        DownloadHandler downloadHandler = DownloadHandlerCreator.create(service.getContentResolver());
        Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(service));
        DownloadExecutorFactory factory = new DownloadExecutorFactory();
        ExecutorService executor = factory.createExecutor();
        DownloadUpdater downloadUpdater = new DownloadUpdater(downloadHandler, executor, pauser, null);

        DownloadObserver downloadObserver = new DownloadObserver(updateHandler, service.getContentResolver());
        return new Delegate(downloadObserver, downloadUpdater, service, updateScheduler);
    }

}
