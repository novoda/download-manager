package com.novoda.downloadmanager;

import java.security.InvalidParameterException;

public interface DownloadBatchStatus {

    enum Status {
        QUEUED,
        DOWNLOADING,
        PAUSED,
        ERROR,
        DELETION,
        DOWNLOADED;

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

    DownloadError.Error getDownloadErrorType();
}
