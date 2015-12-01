package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadRequest;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;

public class Downloader {

    private final DownloadHandler downloadHandler;
    private final Context context;
    private final Pauser pauser;
    private final Listeners listeners;
    private final Watcher watcher;

    public static Downloader from(Context context) {
        Context applicationContext = context.getApplicationContext();
        ContentResolver contentResolver = applicationContext.getContentResolver();
        DatabaseInteraction databaseInteraction = new DatabaseInteraction(contentResolver);
        ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher(new OkHttpClient());
        DownloadHandler downloadHandler = new DownloadHandler(databaseInteraction, contentLengthFetcher);
        Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(context));
        Listeners listeners = Listeners.newInstance();
        Watcher watcher = Watcher.newInstance(context);
        return new Downloader(downloadHandler, applicationContext, pauser, listeners, watcher);
    }

    public Downloader(DownloadHandler downloadHandler, Context context, Pauser pauser, Listeners listeners, Watcher watcher) {
        this.downloadHandler = downloadHandler;
        this.context = context;
        this.pauser = pauser;
        this.listeners = listeners;
        this.watcher = watcher;
    }

    public DownloadId createDownloadId() {
        return downloadHandler.createDownloadId();
    }

    public void submit(DownloadRequest downloadRequest) {
        downloadHandler.submitRequest(downloadRequest);
        context.startService(new Intent(context, Service.class));
    }

    public void pause(DownloadId downloadId) {
        pauser.requestPause(downloadId);
    }

    public void resume(DownloadId downloadId) {
        downloadHandler.resumeDownload(downloadId);
        context.startService(new Intent(context, Service.class));
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
