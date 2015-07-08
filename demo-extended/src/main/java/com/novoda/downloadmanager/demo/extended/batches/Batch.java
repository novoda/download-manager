package com.novoda.downloadmanager.demo.extended.batches;

import com.novoda.downloadmanager.lib.DownloadManager;

public class Batch {
    private final int id;
    private final String title;
    private final int status;
    private final long totalBytes;
    private final long currentBytes;

    public Batch(int id, String title, int status, long totalBytes, long currentBytes) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.totalBytes = totalBytes;
        this.currentBytes = currentBytes;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public String getDownloadStatusText() {
        if (status == DownloadManager.STATUS_RUNNING) {
            return "Downloading";
        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
            return "Complete";
        } else if (status == DownloadManager.STATUS_FAILED) {
            return "Failed";
        } else if (status == DownloadManager.STATUS_PENDING) {
            return "Queued";
        } else if (status == DownloadManager.STATUS_PAUSED) {
            return "Paused";
        } else {
            return "WTH";
        }
    }
}
