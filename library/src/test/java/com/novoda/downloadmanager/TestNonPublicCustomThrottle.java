package com.novoda.downloadmanager;

/**
 * Used to test the {@link CallbackThrottleCreator} to ensure that a
 * {@link IllegalAccessException} is thrown for the custom throttle.
 */
class TestNonPublicCustomThrottle implements CallbackThrottle {

    private TestNonPublicCustomThrottle() {
    }

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
