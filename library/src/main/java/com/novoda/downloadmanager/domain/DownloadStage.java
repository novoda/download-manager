package com.novoda.downloadmanager.domain;

public enum DownloadStage {
    QUEUED,
    SUBMITTED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    MARKED_FOR_DELETION

}
