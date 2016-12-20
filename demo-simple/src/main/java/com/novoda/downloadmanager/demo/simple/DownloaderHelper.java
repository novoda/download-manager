package com.novoda.downloadmanager.demo.simple;

import com.novoda.downloadmanager.Downloader;
import com.novoda.downloadmanager.OnDownloadsChangedListener;
import com.novoda.downloadmanager.OnDownloadsUpdateListener;
import com.novoda.downloadmanager.WatchType;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadRequest;

class DownloaderHelper {

    private final Downloader downloader;

    DownloaderHelper(Downloader downloader) {
        this.downloader = downloader;
    }

    public void startWatching(OnDownloadsUpdateListener onDownloadsUpdate) {
        downloader.addOnDownloadsUpdateListener(onDownloadsUpdate);
        downloader.startListeningForDownloadUpdates(WatchType.PROGRESS, onDownloadsChangedListener);

    }

    public void stopWatching(OnDownloadsUpdateListener onDownloadsUpdate) {
        downloader.removeOnDownloadsUpdateListener(onDownloadsUpdate);
        downloader.stopListeningForDownloadUpdates(onDownloadsChangedListener);
    }

    private final OnDownloadsChangedListener onDownloadsChangedListener = new OnDownloadsChangedListener() {
        @Override
        public void onDownloadsChanged() {
            downloader.requestDownloadsUpdate();
        }
    };

    public void pause(DownloadId id) {
        downloader.pause(id);
    }

    public void resume(DownloadId id) {
        downloader.resume(id);
    }

    public DownloadId createDownloadId() {
        return downloader.createDownloadId();
    }

    public void submit(DownloadRequest downloadRequest) {
        downloader.submit(downloadRequest);
    }

    public void requestDownloadsUpdate() {
        downloader.requestDownloadsUpdate();
    }

    public void delete(DownloadId downloadId) {
        downloader.delete(downloadId);
    }
}
