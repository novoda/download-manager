package com.novoda.downloadmanager;

/**
 * Defines the information that is stored in the persistence layer for a {@link Batch}.
 */
public interface DownloadsBatchPersisted {

    DownloadBatchId downloadBatchId();

    DownloadBatchStatus.Status downloadBatchStatus();

    DownloadBatchTitle downloadBatchTitle();

    long downloadedDateTimeInMillis();

    boolean notificationSeen();

    String storageRoot();
}
