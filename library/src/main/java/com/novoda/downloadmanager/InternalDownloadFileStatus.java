package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

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

    @Nullable
    Optional<DownloadError> error();

}
