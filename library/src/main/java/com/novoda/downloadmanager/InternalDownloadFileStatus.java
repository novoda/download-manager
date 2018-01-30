package com.novoda.downloadmanager;

interface InternalDownloadFileStatus extends DownloadFileStatus {

    void update(FileSize fileSize, FilePath localFilePath);

    boolean isMarkedAsDownloading();

    boolean isMarkedAsQueued();

    boolean isMarkedForDeletion();

    void markAsDownloading();

    void isMarkedAsPaused();

    boolean isMarkedAsError();

    void markAsQueued();

    void markForDeletion();

    void markAsError(DownloadError.Error error);

    Optional<DownloadError> error();

}
