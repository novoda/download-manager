package com.novoda.downloadmanager.demo.extended;

import com.novoda.downloadmanager.lib.DownloadManager;

public class Download {
    private final String title;
    private final String fileName;
    private final int downloadStatus;
    private final long batchId;

    public Download(String title, String fileName, int downloadStatus, long batchId) {
        this.title = title;
        this.fileName = fileName;
        this.downloadStatus = downloadStatus;
        this.batchId = batchId;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadStatusText() {
        if (downloadStatus == DownloadManager.STATUS_RUNNING) {
            return "Downloading";
        } else if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
            return "Complete";
        } else if (downloadStatus == DownloadManager.STATUS_FAILED) {
            return "Failed";
        } else if (downloadStatus == DownloadManager.STATUS_PENDING) {
            return "Queued";
        } else if (downloadStatus == DownloadManager.STATUS_PAUSED) {
            return "Paused";
        } else {
            return "WTH";
        }
    }

    public long getBatchId() {
        return batchId;
    }

    public boolean isPaused() {
        return downloadStatus == DownloadManager.STATUS_PAUSED;
    }

}
