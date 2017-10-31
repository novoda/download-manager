package com.novoda.downloadmanager;

public interface CallbackThrottle {

    void setCallback(DownloadBatchCallback callback);

    void update(DownloadBatchStatus downloadBatchStatus);

    void stopUpdates();
}
