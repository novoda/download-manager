package com.novoda.downloadmanager;

interface InterfaceDownloadFileStatus {

    enum Status {
        PAUSED,
        QUEUED,
        DOWNLOADING,
        DELETION,
        ERROR,
        DOWNLOADED
    }

    DownloadBatchId downloadBatchId();

    DownloadFileId downloadFileId();

    long bytesDownloaded();

    long totalBytes();

    FilePath localFilePath();

    Status status();
}
