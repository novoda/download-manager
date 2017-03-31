package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.client.GlobalClientCheck;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadRequest;
import com.novoda.downloadmanager.download.DownloadDatabaseWrapper;
import com.novoda.downloadmanager.download.DownloadHandlerCreator;

import java.util.List;

public class Downloader {

    private final DownloadDatabaseWrapper downloadDatabaseWrapper;
    private final Pauser pauser;
    private final Listeners listeners;
    private final Watcher watcher;
    private final ServiceStarter serviceStarter;

    public static class Builder {

        private final ServiceBuilder serviceBuilder = new ServiceBuilder();

        public Builder with(GlobalClientCheck globalClientCheck) {
            serviceBuilder.with(globalClientCheck);
            return this;
        }

        public Builder with(DownloadCheck downloadCheck) {
            serviceBuilder.with(downloadCheck);
            return this;
        }

        public Downloader build(Context context) {
            Context applicationContext = context.getApplicationContext();
            ContentResolver contentResolver = applicationContext.getContentResolver();
            DownloadDatabaseWrapper downloadDatabaseWrapper = DownloadHandlerCreator.create(contentResolver);

            Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(context));
            Listeners listeners = Listeners.newInstance();
            Watcher watcher = Watcher.newInstance(context);
            ServiceStarter serviceStarter = serviceBuilder.build(applicationContext);

            return new Downloader(downloadDatabaseWrapper, pauser, listeners, watcher, serviceStarter);
        }

    }

    Downloader(DownloadDatabaseWrapper downloadDatabaseWrapper, Pauser pauser, Listeners listeners, Watcher watcher, ServiceStarter serviceStarter) {
        this.downloadDatabaseWrapper = downloadDatabaseWrapper;
        this.pauser = pauser;
        this.listeners = listeners;
        this.watcher = watcher;
        this.serviceStarter = serviceStarter;
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
        serviceStarter.start();
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
