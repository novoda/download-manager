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

    /**
     * @return The currently downloaded percentage of a {@link DownloadBatchStatus}.
     */
    int percentageDownloaded();

    /**
     * @return The number of bytes that have been downloaded so far.
     */
    long bytesDownloaded();

    /**
     * @return The total number of bytes to download.
     */
    long bytesTotalSize();

    /**
     * @return The unique identifier for this batch.
     */
    DownloadBatchId getDownloadBatchId();

    /**
     * @return The current {@link DownloadBatchStatus.Status} for the batch.
     */
    Status status();

    /**
     * @return The time at which the batch started to download. Represented as the difference,
     * measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
     */
    long downloadedDateTimeInMillis();

    /**
     * @return The {@link DownloadError} or null if {@link DownloadBatchStatus#status()} is not {@link Status#ERROR}.
     */
    @Nullable
    DownloadError downloadError();

    /**
     * @return whether the notification has been dispatched from {@link DownloadBatchStatusNotificationDispatcher}.
     */
    boolean notificationSeen();
}
