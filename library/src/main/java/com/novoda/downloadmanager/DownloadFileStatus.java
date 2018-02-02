package com.novoda.downloadmanager;

public interface DownloadFileStatus {

    enum Status {
        PAUSED,
        QUEUED,
        DOWNLOADING,
        DELETION,
        ERROR,
        DOWNLOADED,
        WAITING_FOR_NETWORK
    }

    DownloadBatchId downloadBatchId();

    DownloadFileId downloadFileId();

    long bytesDownloaded();

    long totalBytes();

    FilePath localFilePath();

    Status status();
}
