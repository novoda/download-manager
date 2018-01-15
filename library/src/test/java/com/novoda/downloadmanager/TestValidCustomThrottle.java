package com.novoda.downloadmanager;

/**
 * Used to test the {@link CallbackThrottleCreator} to ensure that a
 * valid {@link CallbackThrottle} is returned.
 */
public class TestValidCustomThrottle implements CallbackThrottle {

    @Override
    public void setCallback(DownloadBatchStatusCallback callback) {

    }

    @Override
    public void update(DownloadBatchStatus downloadBatchStatus) {

    }

    @Override
    public void stopUpdates() {

    }
}
