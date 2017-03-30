package com.novoda.downloadmanager.domain;

public enum DownloadStage {
    QUEUED,
    SUBMITTED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    MARKED_FOR_DELETION;

    public boolean isActive() {
        return this == DownloadStage.SUBMITTED || this == DownloadStage.RUNNING;
    }
}
