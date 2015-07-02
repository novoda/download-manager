package com.novoda.downloadmanager.demo.extended;

import com.novoda.downloadmanager.lib.DownloadManager;

class Download {
    private final String title;
    private final String fileName;
    private final int downloadStatus;
    private final long id;

    public Download(long id, String title, String fileName, int downloadStatus) {
        this.title = title;
        this.fileName = fileName;
        this.downloadStatus = downloadStatus;
        this.id = id;
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
        return id;
    }

    public boolean isPaused() {
        return downloadStatus == DownloadManager.STATUS_PAUSED;
    }

    public boolean isCompleted() {
        return downloadStatus == DownloadManager.STATUS_SUCCESSFUL;
    }

    public boolean isDownloading() {
        return downloadStatus == DownloadManager.STATUS_RUNNING;
    }
}
