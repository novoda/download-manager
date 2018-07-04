package com.novoda.downloadmanager;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class FileCallbackThrottleCreatorTest {

    @Test
    public void createsTimeThrottle() {
        FileCallbackThrottle fileCallbackThrottle = CallbackThrottleCreator.byTime(TimeUnit.SECONDS, 1)
                .create();

        assertThat(fileCallbackThrottle).isInstanceOf(FileCallbackThrottleByTime.class);
    }

    @Test
    public void createsProgressThrottle() {
        FileCallbackThrottle fileCallbackThrottle = CallbackThrottleCreator.byProgressIncrease()
                .create();

        assertThat(fileCallbackThrottle).isInstanceOf(FileCallbackThrottleByProgressIncrease.class);
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
        FileCallbackThrottle fileCallbackThrottle = CallbackThrottleCreator.byCustomThrottle(TestValidCustomThrottle.class)
                .create();

        assertThat(fileCallbackThrottle).isInstanceOf(TestValidCustomThrottle.class);
    }

    /**
     * Used to test the {@link CallbackThrottleCreator} to ensure that a
     * {@link ClassNotFoundException} is thrown for the custom throttle.
     */
    private static class TestNotFoundCustomThrottle implements FileCallbackThrottle {
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
