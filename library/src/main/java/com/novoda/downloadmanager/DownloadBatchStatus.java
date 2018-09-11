package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

import java.security.InvalidParameterException;

/**
 * Represents the information of a {@link DownloadBatch} that is accessible to clients.
 */
public interface DownloadBatchStatus {

    /**
     * The current download status for a whole batch.
     */
    enum Status {
        QUEUED,
        DOWNLOADING,
        PAUSED,
        ERROR,
        DELETING,
        DELETED,
        DOWNLOADED,
        WAITING_FOR_NETWORK,
        UNKNOWN;

        public String toRawValue() {
            return this.name();
        }

        public static Status from(String rawValue) {
            for (Status status : Status.values()) {
                if (status.name().equals(rawValue)) {
                    return status;
                }
            }

            throw new InvalidParameterException("Batch status " + rawValue + " not supported");
        }

    }

    /**
     * @return The title associated to a {@link DownloadBatch}.
     * Specified when calling {@link Batch#with(StorageRoot, DownloadBatchId, String)}.
     */
    DownloadBatchTitle getDownloadBatchTitle();

    /**
     * Returns the shared storage root for all download files in this batch.
     * The shared storage root for a downloaded file with storage location
     * `/data/user/0/com.novoda.downloadmanager.demo.simple/files/downloads/batch_id_2/20MB.zip`
     * would be `/data/user/0/com.novoda.downloadmanager.demo.simple/files/downloads`.
     *
     * @return The shared storage root.
     */
    String storageRoot();

    int percentageDownloaded();

    long bytesDownloaded();

    long bytesTotalSize();

    DownloadBatchId getDownloadBatchId();

    Status status();

    long downloadedDateTimeInMillis();

    /**
     * @return null if {@link DownloadBatchStatus#status()} is not {@link Status#ERROR}.
     */
    @Nullable
    DownloadError downloadError();

    boolean notificationSeen();
}
