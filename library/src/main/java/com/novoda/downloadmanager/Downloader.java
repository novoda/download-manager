package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadRequest;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;

public class Downloader {

    private final DownloadHandler downloadHandler;
    private final Pauser pauser;
    private final Listeners listeners;
    private final Watcher watcher;
    private final ServiceStarter serviceStarter;

    public static Downloader from(Context context) {
        Context applicationContext = context.getApplicationContext();
        ContentResolver contentResolver = applicationContext.getContentResolver();
        DatabaseInteraction databaseInteraction = new DatabaseInteraction(contentResolver);
        ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher(new OkHttpClient());
        DownloadHandler downloadHandler = new DownloadHandler(databaseInteraction, contentLengthFetcher);
        Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(context));
        Listeners listeners = Listeners.newInstance();
        Watcher watcher = Watcher.newInstance(context);
        ServiceStarter serviceStarter = new ServiceStarter(applicationContext);

        return new Downloader(downloadHandler, pauser, listeners, watcher, serviceStarter);
    }

    public Downloader(DownloadHandler downloadHandler, Pauser pauser, Listeners listeners, Watcher watcher, ServiceStarter serviceStarter) {
        this.downloadHandler = downloadHandler;
        this.pauser = pauser;
        this.listeners = listeners;
        this.watcher = watcher;
        this.serviceStarter = serviceStarter;
    }

    public DownloadId createDownloadId() {
        return downloadHandler.createDownloadId();
    }

    public void submit(DownloadRequest downloadRequest) {
        downloadHandler.submitRequest(downloadRequest);
        serviceStarter.start();
    }

    public void pause(DownloadId downloadId) {
        pauser.requestPause(downloadId);
    }

    public void resume(DownloadId downloadId) {
        downloadHandler.resumeDownload(downloadId);
        serviceStarter.start();
    }

    public List<Download> getAllDownloads() {
        return downloadHandler.getAllDownloads();
    }

    public void addOnDownloadsUpdateListener(OnDownloadsUpdateListener listener) {
        listeners.addOnDownloadsUpdateListener(listener);
    }

    public void removeOnDownloadsUpdateListener(OnDownloadsUpdateListener listener) {
        listeners.removeOnDownloadsUpdateListener(listener);
    }

    public void requestDownloadsUpdate() {
        List<Download> allDownloads = getAllDownloads();
        listeners.notify(allDownloads);
    }

    public void startListeningForDownloadUpdates(WatchType watchType) {
        watcher.startListeningForDownloadUpdates(watchType, onDownloadsChangedListener);
    }

    private final Watcher.OnDownloadsChangedListener onDownloadsChangedListener = new Watcher.OnDownloadsChangedListener() {
        @Override
        public void onDownloadsChanged() {
            requestDownloadsUpdate();
        }
    };

    public void stopListeningForDownloadUpdates() {
        watcher.stopListeningForDownloadUpdates();
    }

    public void addCompletedDownload(DownloadRequest downloadRequest) {
        downloadHandler.addCompletedRequest(downloadRequest);
    }

}
