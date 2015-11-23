package com.novoda.downloadmanager.domain;

public enum DownloadStatus {
    SUBMITTED,
    QUEUED,
    RUNNING,
    PAUSED,
    FAILED,
    MARKED_FOR_DELETION,
    COMPLETED;

    public boolean ignoredFoo() {
        return this == MARKED_FOR_DELETION || this == PAUSED || this == FAILED || this == COMPLETED;
    }

    public boolean isActive() {
        return this == DownloadStatus.SUBMITTED || this == DownloadStatus.RUNNING;
    }
}
