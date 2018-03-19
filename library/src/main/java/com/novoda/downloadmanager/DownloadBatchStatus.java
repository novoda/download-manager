package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

import java.security.InvalidParameterException;

public interface DownloadBatchStatus {

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

    DownloadBatchTitle getDownloadBatchTitle();

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
    DownloadError.Error getDownloadErrorType();

    boolean notificationSeen();
}
