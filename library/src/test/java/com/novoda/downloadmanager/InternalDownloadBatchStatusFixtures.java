package com.novoda.downloadmanager;

class InternalDownloadBatchStatusFixtures {

    private DownloadBatchTitle downloadBatchTitle = DownloadBatchTitleFixtures.aDownloadBatchTitle().build();
    private int percentageDownloaded = 10;
    private long bytesDownloaded = 100;
    private long bytesTotalSize = 1000;
    private DownloadBatchId downloadBatchId = DownloadBatchIdFixtures.aDownloadBatchId().build();
    private DownloadBatchStatus.Status status = DownloadBatchStatus.Status.QUEUED;
    private DownloadError.Error downloadErrorType = null;
    private long downloadedDateTimeInMillis = 123456789L;
    private boolean notificationSeen = false;

    static InternalDownloadBatchStatusFixtures anInternalDownloadsBatchStatus() {
        return new InternalDownloadBatchStatusFixtures();
    }

    InternalDownloadBatchStatusFixtures withDownloadBatchTitle(DownloadBatchTitle downloadBatchTitle) {
        this.downloadBatchTitle = downloadBatchTitle;
        return this;
    }

    InternalDownloadBatchStatusFixtures withPercentageDownloaded(int percentageDownloaded) {
        this.percentageDownloaded = percentageDownloaded;
        return this;
    }

    InternalDownloadBatchStatusFixtures withBytesDownloaded(long bytesDownloaded) {
        this.bytesDownloaded = bytesDownloaded;
        return this;
    }

    InternalDownloadBatchStatusFixtures withBytesTotalSize(long bytesTotalSize) {
        this.bytesTotalSize = bytesTotalSize;
        return this;
    }

    InternalDownloadBatchStatusFixtures withDownloadBatchId(DownloadBatchId downloadBatchId) {
        this.downloadBatchId = downloadBatchId;
        return this;
    }

    InternalDownloadBatchStatusFixtures withStatus(DownloadBatchStatus.Status status) {
        this.status = status;
        return this;
    }

    InternalDownloadBatchStatusFixtures withDownloadedDateTimeInMillis(long downloadedDateTimeInMillis) {
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        return this;
    }

    InternalDownloadBatchStatusFixtures withDownloadErrorType(DownloadError.Error downloadErrorType) {
        this.downloadErrorType = downloadErrorType;
        return this;
    }

    InternalDownloadBatchStatusFixtures withNotificationSeen(boolean notificationSeen) {
        this.notificationSeen = notificationSeen;
        return this;
    }

    InternalDownloadBatchStatus build() {
        return new InternalDownloadBatchStatus() {

            @Override
            public DownloadBatchTitle getDownloadBatchTitle() {
                return downloadBatchTitle;
            }

            @Override
            public int percentageDownloaded() {
                return percentageDownloaded;
            }

            @Override
            public long bytesDownloaded() {
                return bytesDownloaded;
            }

            @Override
            public long bytesTotalSize() {
                return bytesTotalSize;
            }

            @Override
            public DownloadBatchId getDownloadBatchId() {
                return downloadBatchId;
            }

            @Override
            public Status status() {
                return status;
            }

            @Override
            public long downloadedDateTimeInMillis() {
                return downloadedDateTimeInMillis;
            }

            @Override
            public DownloadError.Error getDownloadErrorType() {
                return downloadErrorType;
            }

            @Override
            public void update(long currentBytesDownloaded, long totalBatchSizeBytes) {
                // do nothing.
            }

            @Override
            public void markAsDownloading(DownloadsBatchStatusPersistence persistence) {
                status = Status.DOWNLOADING;
                persistence.updateStatusAsync(downloadBatchId, status);
            }

            @Override
            public void markAsPaused(DownloadsBatchStatusPersistence persistence) {
                status = Status.PAUSED;
                persistence.updateStatusAsync(downloadBatchId, status);
            }

            @Override
            public void markAsQueued(DownloadsBatchStatusPersistence persistence) {
                status = Status.QUEUED;
                persistence.updateStatusAsync(downloadBatchId, status);
            }

            @Override
            public void markAsDeleting() {
                status = Status.DELETING;
            }

            @Override
            public void markAsDeleted() {
                status = Status.DELETED;
            }

            @Override
            public void markAsError(Optional<DownloadError> downloadError, DownloadsBatchStatusPersistence persistence) {
                status = Status.ERROR;
                downloadErrorType = downloadError.get().error();
                persistence.updateStatusAsync(downloadBatchId, status);
            }

            @Override
            public void markAsDownloaded(DownloadsBatchStatusPersistence persistence) {
                status = Status.DOWNLOADED;
                persistence.updateStatusAsync(downloadBatchId, status);
            }

            @Override
            public void markAsWaitingForNetwork(DownloadsBatchPersistence persistence) {
                status = Status.WAITING_FOR_NETWORK;
                persistence.updateStatusAsync(downloadBatchId, status);
            }

            @Override
            public InternalDownloadBatchStatus copy() {
                return build();
            }

            @Override
            public boolean notificationSeen() {
                return notificationSeen;
            }
        };
    }
}
