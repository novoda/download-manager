package com.novoda.downloadmanager;

@SuppressWarnings("PMD.DataClass")
class LiteDownloadsBatchPersisted implements DownloadsBatchPersisted {

    private final DownloadBatchTitle downloadBatchTitle;
    private final DownloadBatchId downloadBatchId;
    private final DownloadBatchStatus.Status status;
    private final long downloadedDateTimeInMillis;
    private final boolean notificationSeen;
    private final String storageRoot;

    LiteDownloadsBatchPersisted(DownloadBatchTitle downloadBatchTitle,
                                DownloadBatchId downloadBatchId,
                                DownloadBatchStatus.Status status,
                                long downloadedDateTimeInMillis,
                                boolean notificationSeen,
                                String storageRoot) {
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadBatchId = downloadBatchId;
        this.status = status;
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        this.notificationSeen = notificationSeen;
        this.storageRoot = storageRoot;
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

    @Override
    public String storageRoot() {
        return storageRoot;
    }
}
