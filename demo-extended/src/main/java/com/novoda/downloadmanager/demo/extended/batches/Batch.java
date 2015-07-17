package com.novoda.downloadmanager.demo.extended.batches;

import com.novoda.downloadmanager.demo.extended.Download;
import com.novoda.downloadmanager.lib.DownloadManager;

/**
 * Model object to encapsulate data from the Batches table.
 * The difference between this and {@link Download} is that the batch doesn't
 * contain the file path as it represents a batch of downloads.
 */
public class Batch {
    private final int id;
    private final String title;
    private final int status;
    private final long totalBytes;
    private final long currentBytes;
    private final String extraData;

    public Batch(int id, String title, int status, long totalBytes, long currentBytes, String extraData) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.totalBytes = totalBytes;
        this.currentBytes = currentBytes;
        this.extraData = extraData;
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
        switch (status) {
            case DownloadManager.STATUS_RUNNING:
                return "Downloading";
            case DownloadManager.STATUS_SUCCESSFUL:
                return "Complete";
            case DownloadManager.STATUS_FAILED:
                return "Failed";
            case DownloadManager.STATUS_PENDING:
                return "Queued";
            case DownloadManager.STATUS_PAUSED:
                return "Paused";
            case DownloadManager.STATUS_DELETING:
                return "Deleting";
            default:
                return "WTH";
        }
    }

    public String getExtraData() {
        return extraData;
    }
}
