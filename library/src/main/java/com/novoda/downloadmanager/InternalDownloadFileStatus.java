package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

interface InternalDownloadFileStatus extends InterfaceDownloadFileStatus {

    void update(FileSize fileSize);

    void update(FilePath filePath);

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
    DownloadError error();

}
