package com.novoda.downloadmanager;

class InternalDownloadBatchStatusFixtures {

    private DownloadBatchTitle downloadBatchTitle = DownloadBatchTitleFixtures.aDownloadBatchTitle().build();
    private int percentageDownloaded = 10;
    private long bytesDownloaded = 100;
    private long bytesTotalSize = 1000;
    private DownloadBatchId downloadBatchId = DownloadBatchIdFixtures.aDownloadBatchId().build();
    private DownloadBatchStatus.Status status = DownloadBatchStatus.Status.QUEUED;
    private DownloadError downloadError = null;
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

    InternalDownloadBatchStatusFixtures withDownloadError(DownloadError downloadError) {
        this.downloadError = downloadError;
        return this;
    }

    InternalDownloadBatchStatusFixtures withNotificationSeen(boolean notificationSeen) {
        this.notificationSeen = notificationSeen;
        return this;
    }

    InternalDownloadBatchStatus build() {
        return new LiteDownloadBatchStatus(
                downloadBatchId,
                downloadBatchTitle,
                downloadedDateTimeInMillis,
                bytesDownloaded,
                bytesTotalSize,
                status,
                notificationSeen,
                Optional.fromNullable(downloadError)
        );
    }
}
