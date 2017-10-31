package com.novoda.downloadmanager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class CallbackThrottleByProgressIncreaseTest {

    private static final int DOWNLOAD_PERCENTAGE = 75;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final DownloadBatchCallback downloadBatchCallback = mock(DownloadBatchCallback.class);
    private final DownloadBatchStatus downloadBatchStatus = mock(DownloadBatchStatus.class);

    private CallbackThrottleByProgressIncrease callbackThrottleByProgressIncrease;

    @Before
    public void setUp() {
        callbackThrottleByProgressIncrease = new CallbackThrottleByProgressIncrease();
    }

    @Test
    public void throwsException_whenStoppingUpdatesWithoutCallback() {
        thrown.expect(NullPointerException.class);

        callbackThrottleByProgressIncrease.stopUpdates();
    }

    @Test
    public void doesNothing_whenUpdatingWithoutCallback() {
        callbackThrottleByProgressIncrease.update(downloadBatchStatus);

        verifyZeroInteractions(downloadBatchCallback, downloadBatchStatus);
    }

    @Test
    public void doesNotEmit_whenPercentageIsUnchanged() {
        givenStatusUpdate(downloadBatchStatus);

        callbackThrottleByProgressIncrease.update(downloadBatchStatus);

        verifyZeroInteractions(downloadBatchCallback);
    }

    @Test
    public void emitsLastStatus_whenStoppingUpdates() {
        givenStatusUpdate(downloadBatchStatus);

        callbackThrottleByProgressIncrease.stopUpdates();

        verify(downloadBatchCallback).onUpdate(downloadBatchStatus);
    }

    private void givenStatusUpdate(DownloadBatchStatus downloadBatchStatus) {
        given(downloadBatchStatus.percentageDownloaded()).willReturn(DOWNLOAD_PERCENTAGE);
        callbackThrottleByProgressIncrease.setCallback(downloadBatchCallback);
        callbackThrottleByProgressIncrease.update(downloadBatchStatus);
        reset(downloadBatchCallback);
    }
}
