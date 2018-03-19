package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

class LiteDownloadBatchStatus implements InternalDownloadBatchStatus {

    private static final long ZERO_BYTES = 0;
    private static final int TOTAL_PERCENTAGE = 100;

    private final DownloadBatchTitle downloadBatchTitle;
    private final DownloadBatchId downloadBatchId;
    private final long downloadedDateTimeInMillis;

    private Status status;
    private boolean notificationSeen;
    private long bytesDownloaded;
    private long totalBatchSizeBytes;
    private int percentageDownloaded;
    private Optional<DownloadError> downloadError;

    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
    LiteDownloadBatchStatus(DownloadBatchId downloadBatchId,
                            DownloadBatchTitle downloadBatchTitle,
                            long downloadedDateTimeInMillis,
                            long bytesDownloaded,
                            long totalBatchSizeBytes,
                            Status status,
                            boolean notificationSeen,
                            Optional<DownloadError> downloadError) {
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadBatchId = downloadBatchId;
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        this.bytesDownloaded = bytesDownloaded;
        this.totalBatchSizeBytes = totalBatchSizeBytes;
        this.downloadError = downloadError;
        this.percentageDownloaded = getPercentageFrom(bytesDownloaded, totalBatchSizeBytes);
        this.status = status;
        this.notificationSeen = notificationSeen;
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
        updateStatusAsync(status, persistence);
    }

    @Override
    public void markAsPaused(DownloadsBatchStatusPersistence persistence) {
        status = Status.PAUSED;
        updateStatusAsync(status, persistence);
    }

    @Override
    public void markAsQueued(DownloadsBatchStatusPersistence persistence) {
        status = Status.QUEUED;
        updateStatusAsync(status, persistence);
    }

    @Override
    public void markAsDeleting() {
        status = Status.DELETING;
        notificationSeen = false;
    }

    @Override
    public void markAsDeleted() {
        status = Status.DELETED;
        notificationSeen = false;
    }

    @Override
    public void markAsError(Optional<DownloadError> downloadError, DownloadsBatchStatusPersistence persistence) {
        this.status = Status.ERROR;
        this.downloadError = downloadError;
        updateStatusAsync(status, persistence);
    }

    @Override
    public void markAsDownloaded(DownloadsBatchStatusPersistence persistence) {
        this.status = Status.DOWNLOADED;
        updateStatusAsync(status, persistence);
    }

    @Override
    public void markAsWaitingForNetwork(DownloadsBatchPersistence persistence) {
        this.status = Status.WAITING_FOR_NETWORK;
        updateStatusAsync(status, persistence);
    }

    @Override
    public InternalDownloadBatchStatus copy() {
        return new LiteDownloadBatchStatus(
                downloadBatchId,
                downloadBatchTitle,
                downloadedDateTimeInMillis,
                bytesDownloaded,
                totalBatchSizeBytes,
                status,
                notificationSeen,
                downloadError
        );
    }

    private void updateStatusAsync(Status status, DownloadsBatchStatusPersistence persistence) {
        persistence.updateStatusAsync(downloadBatchId, status);
    }

    @Nullable
    @Override
    public DownloadError.Error getDownloadErrorType() {
        if (downloadError.isPresent()) {
            return downloadError.get().error();
        } else {
            return null;
        }
    }

    @Override
    public boolean notificationSeen() {
        return notificationSeen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LiteDownloadBatchStatus that = (LiteDownloadBatchStatus) o;

        if (downloadedDateTimeInMillis != that.downloadedDateTimeInMillis) {
            return false;
        }
        if (bytesDownloaded != that.bytesDownloaded) {
            return false;
        }
        if (totalBatchSizeBytes != that.totalBatchSizeBytes) {
            return false;
        }
        if (percentageDownloaded != that.percentageDownloaded) {
            return false;
        }
        if (notificationSeen != that.notificationSeen) {
            return false;
        }
        if (downloadBatchTitle != null ? !downloadBatchTitle.equals(that.downloadBatchTitle) : that.downloadBatchTitle != null) {
            return false;
        }
        if (downloadBatchId != null ? !downloadBatchId.equals(that.downloadBatchId) : that.downloadBatchId != null) {
            return false;
        }
        if (status != that.status) {
            return false;
        }
        return downloadError != null ? downloadError.equals(that.downloadError) : that.downloadError == null;
    }

    @Override
    public int hashCode() {
        int result = downloadBatchTitle != null ? downloadBatchTitle.hashCode() : 0;
        result = 31 * result + (downloadBatchId != null ? downloadBatchId.hashCode() : 0);
        result = 31 * result + (int) (downloadedDateTimeInMillis ^ (downloadedDateTimeInMillis >>> 32));
        result = 31 * result + (int) (bytesDownloaded ^ (bytesDownloaded >>> 32));
        result = 31 * result + (int) (totalBatchSizeBytes ^ (totalBatchSizeBytes >>> 32));
        result = 31 * result + percentageDownloaded;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (notificationSeen ? 1 : 0);
        result = 31 * result + (downloadError != null ? downloadError.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LiteDownloadBatchStatus{"
                + "downloadBatchTitle=" + downloadBatchTitle
                + ", downloadBatchId=" + downloadBatchId
                + ", downloadedDateTimeInMillis=" + downloadedDateTimeInMillis
                + ", bytesDownloaded=" + bytesDownloaded
                + ", totalBatchSizeBytes=" + totalBatchSizeBytes
                + ", percentageDownloaded=" + percentageDownloaded
                + ", status=" + status
                + ", notificationSeen=" + notificationSeen
                + ", downloadError=" + downloadError
                + '}';
    }
}
