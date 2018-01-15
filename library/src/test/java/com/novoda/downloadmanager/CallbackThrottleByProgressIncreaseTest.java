package com.novoda.downloadmanager;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class CallbackThrottleByProgressIncreaseTest {

    private static final int DOWNLOAD_PERCENTAGE = 75;

    private final DownloadBatchStatusCallback downloadBatchCallback = mock(DownloadBatchStatusCallback.class);
    private final DownloadBatchStatus downloadBatchStatus = mock(DownloadBatchStatus.class);

    private CallbackThrottleByProgressIncrease callbackThrottleByProgressIncrease;

    @Before
    public void setUp() {
        callbackThrottleByProgressIncrease = new CallbackThrottleByProgressIncrease();
    }

    @Test(expected = NullPointerException.class)
    public void throwsException_whenStoppingUpdatesWithoutCallback() {
        callbackThrottleByProgressIncrease.stopUpdates();
    }

    @Test
    public void doesNotEmit_whenPercentageIsUnchanged() {
        callbackThrottleByProgressIncrease.setCallback(downloadBatchCallback);
        givenPreviousUpdate(downloadBatchStatus);

        callbackThrottleByProgressIncrease.update(downloadBatchStatus);

        verifyZeroInteractions(downloadBatchCallback);
    }

    @Test
    public void emitsLastStatus_whenStoppingUpdates() {
        callbackThrottleByProgressIncrease.setCallback(downloadBatchCallback);
        givenPreviousUpdate(downloadBatchStatus);

        callbackThrottleByProgressIncrease.stopUpdates();

        verify(downloadBatchCallback).onUpdate(downloadBatchStatus);
    }

    private void givenPreviousUpdate(DownloadBatchStatus downloadBatchStatus) {
        given(downloadBatchStatus.percentageDownloaded()).willReturn(DOWNLOAD_PERCENTAGE);
        callbackThrottleByProgressIncrease.update(downloadBatchStatus);
        reset(downloadBatchCallback);
    }
}
