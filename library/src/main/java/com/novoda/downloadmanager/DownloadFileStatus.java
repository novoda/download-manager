package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

class DownloadFileStatus {

    enum Status {
        PAUSED,
        QUEUED,
        DOWNLOADING,
        DELETION,
        ERROR,
        DOWNLOADED
    }

    private final DownloadFileId downloadFileId;

    private FileSize fileSize;
    private Status status;
    private DownloadError downloadError;

    DownloadFileStatus(DownloadFileId downloadFileId, Status status, FileSize fileSize) {
        this.downloadFileId = downloadFileId;
        this.status = status;
        this.fileSize = fileSize;
    }

    void update(FileSize fileSize) {
        this.fileSize = fileSize;
    }

    long bytesDownloaded() {
        return fileSize.currentSize();
    }

    DownloadFileId getDownloadFileId() {
        return downloadFileId;
    }

    boolean isMarkedAsDownloading() {
        return status == Status.DOWNLOADING;
    }

    boolean isMarkedAsQueued() {
        return status == Status.QUEUED;
    }

    boolean isMarkedForDeletion() {
        return status == Status.DELETION;
    }

    void markAsDownloading() {
        status = Status.DOWNLOADING;
    }

    void isMarkedAsPaused() {
        status = Status.PAUSED;
    }

    boolean isMarkedAsError() {
        return status == Status.ERROR && downloadError != null;
    }

    void markAsQueued() {
        status = Status.QUEUED;
    }

    void markForDeletion() {
        status = Status.DELETION;
    }

    void markAsError(DownloadError.Error error) {
        status = Status.ERROR;
        downloadError = new DownloadError(error);
    }

    @Nullable
    DownloadError getError() {
        return downloadError;
    }

    Status getStatus() {
        return status;
    }
}
