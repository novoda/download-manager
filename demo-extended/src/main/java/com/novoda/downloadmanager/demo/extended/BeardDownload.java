package com.novoda.downloadmanager.demo.extended;

import com.novoda.downloadmanager.lib.DownloadManager;

/**
 * Model object to encapsulate data from the Downloads table.
 * It represents a single download containing the file name of the downloaded file.
 */
public class BeardDownload {
    private final String title;
    private final String fileName;
    private final int downloadStatus;
    private final long batchId;

    public BeardDownload(String title, String fileName, int downloadStatus, long batchId) {
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
        switch (downloadStatus) {
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

    public long getBatchId() {
        return batchId;
    }

    public boolean isPaused() {
        return downloadStatus == DownloadManager.STATUS_PAUSED;
    }

}
