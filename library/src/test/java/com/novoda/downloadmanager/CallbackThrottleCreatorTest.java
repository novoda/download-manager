package com.novoda.downloadmanager;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class CallbackThrottleCreatorTest {

    @Test
    public void createsTimeThrottle() {
        CallbackThrottle callbackThrottle = CallbackThrottleCreator.byTime(TimeUnit.SECONDS, 1)
                .create();

        assertThat(callbackThrottle).isInstanceOf(CallbackThrottleByTime.class);
    }

    @Test
    public void createsProgressThrottle() {
        CallbackThrottle callbackThrottle = CallbackThrottleCreator.byProgressIncrease()
                .create();

        assertThat(callbackThrottle).isInstanceOf(CallbackThrottleByProgressIncrease.class);
    }

    @Test(expected = RuntimeException.class)
    public void throwsException_whenCallbackThrottleDoesNotExist() {
        CallbackThrottleCreator.byCustomThrottle(null)
                .create();
    }

    @Test(expected = RuntimeException.class)
    public void throwsException_whenCustomCallbackIsNotPublic() {
        CallbackThrottleCreator.byCustomThrottle(TestNonPublicCustomThrottle.class)
                .create();
    }

    @Test(expected = RuntimeException.class)
    public void throwsException_whenCustomCallbackIsNotFound() {
        CallbackThrottleCreator.byCustomThrottle(TestNotFoundCustomThrottle.class)
                .create();
    }

    @Test(expected = RuntimeException.class)
    public void throwsException_whenCustomCallbackIsNotInstantiable() {
        CallbackThrottleCreator.byCustomThrottle(TestNonInstantiableCustomThrottle.class)
                .create();
    }

    @Test
    public void createsCustomThrottle() {
        CallbackThrottle callbackThrottle = CallbackThrottleCreator.byCustomThrottle(TestValidCustomThrottle.class)
                .create();

        assertThat(callbackThrottle).isInstanceOf(TestValidCustomThrottle.class);
    }

    /**
     * Used to test the {@link CallbackThrottleCreator} to ensure that a
     * {@link ClassNotFoundException} is thrown for the custom throttle.
     */
    private static class TestNotFoundCustomThrottle implements CallbackThrottle {
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

}
