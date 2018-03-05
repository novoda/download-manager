package com.novoda.downloadmanager;

class LiteDownloadFileStatus implements InternalDownloadFileStatus {

    private final DownloadBatchId downloadBatchId;
    private final DownloadFileId downloadFileId;

    private FileSize fileSize;
    private FilePath localFilePath;
    private Status status;
    private Optional<DownloadError> downloadError = Optional.absent();

    LiteDownloadFileStatus(DownloadBatchId downloadBatchId, DownloadFileId downloadFileId, Status status, FileSize fileSize, FilePath localFilePath) {
        this.downloadBatchId = downloadBatchId;
        this.downloadFileId = downloadFileId;
        this.status = status;
        this.fileSize = fileSize;
        this.localFilePath = localFilePath;
    }

    @Override
    public void update(FileSize fileSize, FilePath localFilePath) {
        this.fileSize = fileSize;
        this.localFilePath = localFilePath;

        if (fileSize.currentSize() == fileSize.totalSize()) {
            markAsDownloaded();
        }
    }

    private void markAsDownloaded() {
        status = Status.DOWNLOADED;
    }

    @Override
    public long bytesDownloaded() {
        return fileSize.currentSize();
    }

    @Override
    public long totalBytes() {
        return fileSize.totalSize();
    }

    @Override
    public FilePath localFilePath() {
        return localFilePath;
    }

    @Override
    public DownloadBatchId downloadBatchId() {
        return downloadBatchId;
    }

    @Override
    public DownloadFileId downloadFileId() {
        return downloadFileId;
    }

    @Override
    public boolean isMarkedAsDownloading() {
        return status == Status.DOWNLOADING;
    }

    @Override
    public boolean isMarkedAsQueued() {
        return status == Status.QUEUED;
    }

    @Override
    public boolean isMarkedAsDeleted() {
        return status == Status.DELETED;
    }

    @Override
    public void markAsDownloading() {
        status = Status.DOWNLOADING;
    }

    @Override
    public void markAsPaused() {
        status = Status.PAUSED;
    }

    @Override
    public boolean isMarkedAsError() {
        return status == Status.ERROR;
    }

    @Override
    public void markAsQueued() {
        status = Status.QUEUED;
    }

    @Override
    public void markAsDeleted() {
        status = Status.DELETED;
    }

    @Override
    public void markAsError(DownloadError.Error error) {
        status = Status.ERROR;
        downloadError = Optional.of(new DownloadError(error));
    }

    @Override
    public boolean isMarkedAsWaitingForNetwork() {
        return status == Status.WAITING_FOR_NETWORK;
    }

    @Override
    public void waitForNetwork() {
        status = Status.WAITING_FOR_NETWORK;
    }

    @Override
    public Optional<DownloadError> error() {
        return downloadError;
    }

    @Override
    public Status status() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LiteDownloadFileStatus that = (LiteDownloadFileStatus) o;

        if (downloadBatchId != null ? !downloadBatchId.equals(that.downloadBatchId) : that.downloadBatchId != null) {
            return false;
        }
        if (downloadFileId != null ? !downloadFileId.equals(that.downloadFileId) : that.downloadFileId != null) {
            return false;
        }
        if (fileSize != null ? !fileSize.equals(that.fileSize) : that.fileSize != null) {
            return false;
        }
        if (localFilePath != null ? !localFilePath.equals(that.localFilePath) : that.localFilePath != null) {
            return false;
        }
        if (status != that.status) {
            return false;
        }
        return downloadError != null ? downloadError.equals(that.downloadError) : that.downloadError == null;
    }

    @Override
    public int hashCode() {
        int result = downloadBatchId != null ? downloadBatchId.hashCode() : 0;
        result = 31 * result + (downloadFileId != null ? downloadFileId.hashCode() : 0);
        result = 31 * result + (fileSize != null ? fileSize.hashCode() : 0);
        result = 31 * result + (localFilePath != null ? localFilePath.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (downloadError != null ? downloadError.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LiteDownloadFileStatus{"
                + "downloadBatchId=" + downloadBatchId
                + ", downloadFileId=" + downloadFileId
                + ", fileSize=" + fileSize
                + ", localFilePath=" + localFilePath
                + ", status=" + status
                + ", downloadError=" + downloadError
                + '}';
    }
}
