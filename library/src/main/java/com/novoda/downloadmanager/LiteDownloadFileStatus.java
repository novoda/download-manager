package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

class LiteDownloadFileStatus implements InternalDownloadFileStatus {

    private final DownloadBatchId downloadBatchId;
    private final DownloadFileId downloadFileId;

    private FileSize fileSize;
    private FilePath localFilePath;
    private Status status;
    private DownloadError downloadError;

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
    public boolean isMarkedForDeletion() {
        return status == Status.DELETION;
    }

    @Override
    public void markAsDownloading() {
        status = Status.DOWNLOADING;
    }

    @Override
    public void isMarkedAsPaused() {
        status = Status.PAUSED;
    }

    @Override
    public boolean isMarkedAsError() {
        return status == Status.ERROR && downloadError != null;
    }

    @Override
    public void markAsQueued() {
        status = Status.QUEUED;
    }

    @Override
    public void markForDeletion() {
        status = Status.DELETION;
    }

    @Override
    public void markAsError(DownloadError.Error error) {
        status = Status.ERROR;
        downloadError = new DownloadError(error);
    }

    @Override
    @Nullable
    public DownloadError error() {
        return downloadError;
    }

    @Override
    public Status status() {
        return status;
    }

    @Override
    public String toString() {
        return "LiteDownloadFileStatus{" +
                "downloadBatchId=" + downloadBatchId +
                ", downloadFileId=" + downloadFileId +
                ", fileSize=" + fileSize +
                ", localFilePath=" + localFilePath +
                ", status=" + status +
                ", downloadError=" + downloadError +
                '}';
    }
}
