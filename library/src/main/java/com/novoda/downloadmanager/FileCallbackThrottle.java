package com.novoda.downloadmanager;

/**
 * Controls the rate at which a {@link DownloadFile} calls back to
 * a {@link DownloadBatch}. Essentially controls the rate of notifications
 * during a download of a file.
 * <p>
 * Note: This does not control the overall emissions of {@link DownloadBatchStatus} between the client and this library.
 */
public interface FileCallbackThrottle {

    /**
     * Set the callback that should be notified of {@link DownloadBatchStatus}
     * changes.
     *
     * @param callback to notify.
     */
    void setCallback(DownloadBatchStatusCallback callback);

    /**
     * The throttle logic that will forward events to the {@link DownloadBatchStatusCallback}.
     *
     * @param downloadBatchStatus the latest status to emit.
     */
    void update(DownloadBatchStatus downloadBatchStatus);

    /**
     * Disable the callback or the throttling logic to
     * prevent further emissions of {@link DownloadBatchStatus}.
     */
    void stopUpdates();
}
