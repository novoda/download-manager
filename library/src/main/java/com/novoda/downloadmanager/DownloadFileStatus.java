package com.novoda.downloadmanager;

public interface DownloadFileStatus {

    enum Status {
        PAUSED,
        QUEUED,
        DOWNLOADING,
        DELETION,
        ERROR,
        DOWNLOADED
    }

    DownloadBatchId downloadBatchId();

    LiteDownloadFileId downloadFileId();

    long bytesDownloaded();

    long totalBytes();

    FilePath localFilePath();

    Status status();
}
