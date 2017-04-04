package com.novoda.downloadmanager;

public enum DownloadStatus {
    QUEUED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    TRANSITIONING;

    public static DownloadStatus from(DownloadStage stage) {
        switch (stage) {
            case QUEUED:
            case SUBMITTED:
                return QUEUED;

            case RUNNING:
                return RUNNING;

            case PAUSED:
                return PAUSED;

            case COMPLETED:
                return COMPLETED;

            case FAILED:
                return FAILED;

            case MARKED_FOR_DELETION:
                return TRANSITIONING;

        }
        throw new IllegalArgumentException("Does not handle stage : " + stage.name());
    }

}
