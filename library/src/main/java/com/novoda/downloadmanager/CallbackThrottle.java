package com.novoda.downloadmanager;

/**
 * For defining the speed at which {@link DownloadBatchStatus} notifications are emitted.
 */
public interface CallbackThrottle {

    /**
     * The callback to pass each emission of {@link DownloadBatchStatus} to.
     *
     * @param callback to notify.
     */
    void setCallback(DownloadBatchStatusCallback callback);

    /**
     * Called internally by the download manager each time a {@link DownloadBatchStatus} changes.
     * Clients should control the throttling of this status.
     *
     * @param downloadBatchStatus to throttle.
     */
    void update(DownloadBatchStatus downloadBatchStatus);

    /**
     * Prevent additional {@link DownloadBatchStatus} from being emitted.
     */
    void stopUpdates();
}
