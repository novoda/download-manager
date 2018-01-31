package com.novoda.downloadmanager;

class LiteDownloadsBatchPersisted implements DownloadsBatchPersisted {

    private final DownloadBatchTitle downloadBatchTitle;
    private final DownloadBatchId downloadBatchId;
    private final DownloadBatchStatus.Status status;
    private final long downloadedDateTimeInMillis;
    private final boolean notificationSeen;

    LiteDownloadsBatchPersisted(DownloadBatchTitle downloadBatchTitle,
                                DownloadBatchId downloadBatchId,
                                DownloadBatchStatus.Status status,
                                long downloadedDateTimeInMillis,
                                boolean notificationSeen) {
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadBatchId = downloadBatchId;
        this.status = status;
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        this.notificationSeen = notificationSeen;
    }

    @Override
    public DownloadBatchId downloadBatchId() {
        return downloadBatchId;
    }

    @Override
    public DownloadBatchStatus.Status downloadBatchStatus() {
        return status;
    }

    @Override
    public DownloadBatchTitle downloadBatchTitle() {
        return downloadBatchTitle;
    }

    @Override
    public long downloadedDateTimeInMillis() {
        return downloadedDateTimeInMillis;
    }

    @Override
    public boolean notificationSeen() {
        return notificationSeen;
    }
}
