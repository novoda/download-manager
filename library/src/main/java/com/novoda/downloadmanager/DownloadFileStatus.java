package com.novoda.downloadmanager;

/**
 * Represents the information of a {@link DownloadFile} that is accessible to clients.
 */
public interface DownloadFileStatus {

    /**
     * The current download status for a file.
     */
    enum Status {
        PAUSED,
        QUEUED,
        DOWNLOADING,
        DELETED,
        ERROR,
        DOWNLOADED,
        WAITING_FOR_NETWORK
    }

    /**
     * @return The unique identifier for the batch associated with this file.
     */
    DownloadBatchId downloadBatchId();

    /**
     * @return The unique identifier for this file.
     */
    DownloadFileId downloadFileId();

    /**
     * @return The number of bytes that have been downloaded so far.
     */
    long bytesDownloaded();

    /**
     * @return The total number of bytes to download.
     */
    long totalBytes();

    /**
     * @return The local path to this download file.
     */
    FilePath localFilePath();

    /**
     * @return The current {@link DownloadFileStatus.Status} for the file.
     */
    Status status();
}
