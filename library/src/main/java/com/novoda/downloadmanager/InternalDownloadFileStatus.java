package com.novoda.downloadmanager;

interface InternalDownloadFileStatus extends DownloadFileStatus {

    void update(FileSize fileSize, FilePath localFilePath);

    boolean isMarkedAsDownloading();

    boolean isMarkedAsQueued();

    boolean isMarkedAsDeleted();

    void markAsDownloading();

    void markAsPaused();

    boolean isMarkedAsError();

    void markAsQueued();

    void markAsDeleted();

    void markAsError(DownloadError.Type type);

    boolean isMarkedAsWaitingForNetwork();

    void waitForNetwork();

    Optional<DownloadError> error();
}
