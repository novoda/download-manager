package com.novoda.downloadmanager;

public interface CallbackThrottle {

    void setCallback(DownloadBatchStatusCallback callback);

    void update(DownloadBatchStatus downloadBatchStatus);

    void stopUpdates();
}
