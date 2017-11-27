package com.novoda.downloadmanager.demo.simple;

import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadBatchTitle;

class BeardDownload {
    private final DownloadBatchTitle title;
    private final DownloadBatchStatus.Status downloadStatus;

    public BeardDownload(DownloadBatchTitle title, DownloadBatchStatus.Status downloadStatus) {
        this.title = title;
        this.downloadStatus = downloadStatus;
    }

    public String getTitle() {
        return title.asString();
    }

    public String getFileName() {
        return " ... Not sure about the file name yet";
    }

    public String getDownloadStatusText() {
        if (downloadStatus == DownloadBatchStatus.Status.DOWNLOADING) {
            return "Downloading";
        } else if (downloadStatus == DownloadBatchStatus.Status.DOWNLOADED) {
            return "Complete";
        } else if (downloadStatus == DownloadBatchStatus.Status.ERROR) {
            return "Failed";
        } else if (downloadStatus == DownloadBatchStatus.Status.QUEUED) {
            return "Queued";
        } else if (downloadStatus == DownloadBatchStatus.Status.PAUSED) {
            return "Paused";
        } else if (downloadStatus == DownloadBatchStatus.Status.DELETION) {
            return "Deleting";
        } else {
            return "WTH";
        }
    }
}
