package com.novoda.downloadmanager.domain;

public enum DownloadStage {
    QUEUED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    MARKED_FOR_DELETION,
    SUBMITTED

}
