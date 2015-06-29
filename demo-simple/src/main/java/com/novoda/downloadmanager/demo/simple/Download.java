package com.novoda.downloadmanager.demo.simple;

import com.novoda.downloadmanager.lib.DownloadManager;

class Download {
    private final String title;
    private final String fileName;
    private final int downloadStatus;

    public Download(String title, String fileName, int downloadStatus) {
        this.title = title;
        this.fileName = fileName;
        this.downloadStatus = downloadStatus;
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
}
