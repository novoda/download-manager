package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import java.util.List;

public class Downloader {

    private final DownloadDatabaseWrapper downloadDatabaseWrapper;
    private final Pauser pauser;
    private final Listeners listeners;
    private final Watcher watcher;
    private final DownloadServiceConnection downloadServiceConnection;

    public static class Builder {

        private final DownloadServiceConnectionBuilder downloadServiceConnectionBuilder = new DownloadServiceConnectionBuilder();

        public Builder with(GlobalClientCheck globalClientCheck) {
            downloadServiceConnectionBuilder.with(globalClientCheck);
            return this;
        }

        public Builder with(DownloadCheck downloadCheck) {
            downloadServiceConnectionBuilder.with(downloadCheck);
            return this;
        }

        public Downloader build(Context context) {
            Context applicationContext = context.getApplicationContext();
            ContentResolver contentResolver = applicationContext.getContentResolver();
            DownloadDatabaseWrapper downloadDatabaseWrapper = DownloadDatabaseWrapperCreator.create(contentResolver);

            Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(context));
            Listeners listeners = Listeners.newInstance();
            Watcher watcher = Watcher.newInstance(context);
            DownloadServiceConnection downloadServiceConnection = downloadServiceConnectionBuilder.build(applicationContext);

            return new Downloader(downloadDatabaseWrapper, pauser, listeners, watcher, downloadServiceConnection);
        }

    }

    Downloader(DownloadDatabaseWrapper downloadDatabaseWrapper, Pauser pauser, Listeners listeners, Watcher watcher, DownloadServiceConnection downloadServiceConnection) {
        this.downloadDatabaseWrapper = downloadDatabaseWrapper;
        this.pauser = pauser;
        this.listeners = listeners;
        this.watcher = watcher;
        this.downloadServiceConnection = downloadServiceConnection;
    }

    public DownloadId createDownloadId() {
        return downloadDatabaseWrapper.createDownloadId();
    }

    public void submit(DownloadRequest downloadRequest) {
        downloadDatabaseWrapper.submitRequest(downloadRequest);
        startService();
    }

    public void delete(DownloadId downloadId) {
        downloadDatabaseWrapper.markForDeletion(downloadId);
        startService();
    }

    public void pause(DownloadId downloadId) {
        pauser.requestPause(downloadId);
    }

    public void resume(DownloadId downloadId) {
        downloadDatabaseWrapper.resumeDownload(downloadId);
        startService();
    }

    private void startService() {
        downloadServiceConnection.startService();
    }

    public List<Download> getAllDownloads() {
        return downloadDatabaseWrapper.getAllDownloads();
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

    public void startListeningForDownloadUpdates(WatchType watchType, OnDownloadsChangedListener onDownloadsChangedListener) {
        watcher.startListeningForDownloadUpdates(watchType, onDownloadsChangedListener);
    }

    public void stopListeningForDownloadUpdates(OnDownloadsChangedListener onDownloadsChangedListener) {
        watcher.stopListeningForDownloadUpdates(onDownloadsChangedListener);
    }

    public void addCompletedDownload(DownloadRequest downloadRequest) {
        downloadDatabaseWrapper.addCompletedRequest(downloadRequest);
    }

    public void forceStart() {
        startService();
    }

}
