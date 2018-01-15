package com.novoda.downloadmanager;

/**
 * Used to test the {@link CallbackThrottleCreator} to ensure that a
 * {@link InstantiationException} is thrown for the custom throttle.
 */
abstract class TestNonInstantiableCustomThrottle implements CallbackThrottle {

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
