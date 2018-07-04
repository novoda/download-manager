package com.novoda.downloadmanager;

public interface FileCallbackThrottle {

    void setCallback(DownloadBatchStatusCallback callback);

    void update(DownloadBatchStatus downloadBatchStatus);

    void stopUpdates();
}
