package com.novoda.downloadmanager;

class LiteDownloadBatchStatus implements InternalDownloadBatchStatus {

    private static final long ZERO_BYTES = 0;
    private static final int TOTAL_PERCENTAGE = 100;

    private final DownloadBatchTitle downloadBatchTitle;
    private final DownloadBatchId downloadBatchId;
    private final long downloadedDateTimeInMillis;

    private long bytesDownloaded;
    private long totalBatchSizeBytes;
    private int percentageDownloaded;
    private Status status;

    private Optional<DownloadError> downloadError = Optional.absent();

    LiteDownloadBatchStatus(DownloadBatchId downloadBatchId, DownloadBatchTitle downloadBatchTitle, long downloadedDateTimeInMillis, Status status) {
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadBatchId = downloadBatchId;
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        this.status = status;
    }

    @Override
    public long bytesDownloaded() {
        return bytesDownloaded;
    }

    @Override
    public long bytesTotalSize() {
        return totalBatchSizeBytes;
    }

    @Override
    public void update(long currentBytesDownloaded, long totalBatchSizeBytes) {
        this.bytesDownloaded = currentBytesDownloaded;
        this.totalBatchSizeBytes = totalBatchSizeBytes;
        this.percentageDownloaded = getPercentageFrom(bytesDownloaded, totalBatchSizeBytes);
    }

    private int getPercentageFrom(long bytesDownloaded, long totalFileSizeBytes) {
        if (totalBatchSizeBytes <= ZERO_BYTES) {
            return 0;
        } else {
            return (int) ((((float) bytesDownloaded) / ((float) totalFileSizeBytes)) * TOTAL_PERCENTAGE);
        }
    }

    @Override
    public int percentageDownloaded() {
        return percentageDownloaded;
    }

    @Override
    public DownloadBatchId getDownloadBatchId() {
        return downloadBatchId;
    }

    @Override
    public DownloadBatchTitle getDownloadBatchTitle() {
        return downloadBatchTitle;
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
    public void markAsDownloading(DownloadsBatchStatusPersistence persistence) {
        status = Status.DOWNLOADING;
        updateStatus(status, persistence);
    }

    @Override
    public void markAsPaused(DownloadsBatchStatusPersistence persistence) {
        status = Status.PAUSED;
        updateStatus(status, persistence);
    }

    @Override
    public void markAsQueued(DownloadsBatchStatusPersistence persistence) {
        status = Status.QUEUED;
        updateStatus(status, persistence);
    }

    @Override
    public void markForDeletion() {
        status = Status.DELETION;
    }

    @Override
    public void markAsError(Optional<DownloadError> downloadError, DownloadsBatchStatusPersistence persistence) {
        this.status = Status.ERROR;
        this.downloadError = downloadError;
        updateStatus(status, persistence);
    }

    @Override
    public void markAsDownloaded(DownloadsBatchStatusPersistence persistence) {
        this.status = Status.DOWNLOADED;
        updateStatus(status, persistence);
    }

    private void updateStatus(Status status, DownloadsBatchStatusPersistence persistence) {
        persistence.updateStatusAsync(downloadBatchId, status);
    }

    @Override
    public DownloadError.Error getDownloadErrorType() {
        return downloadError.or(new DownloadError(DownloadError.Error.UNKNOWN)).error();
    }
}
