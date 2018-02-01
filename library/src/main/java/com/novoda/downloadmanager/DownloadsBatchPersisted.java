package com.novoda.downloadmanager;

public interface DownloadsBatchPersisted {

    DownloadBatchId downloadBatchId();

    DownloadBatchStatus.Status downloadBatchStatus();

    DownloadBatchTitle downloadBatchTitle();

    long downloadedDateTimeInMillis();

    boolean notificationSeen();
}
