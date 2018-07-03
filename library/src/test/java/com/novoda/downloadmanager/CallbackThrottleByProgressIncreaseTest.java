package com.novoda.downloadmanager;

import org.junit.Test;

import static com.novoda.downloadmanager.InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class CallbackThrottleByProgressIncreaseTest {

    private static final int DOWNLOAD_PERCENTAGE = 75;

    private final DownloadBatchStatusCallback downloadBatchCallback = mock(DownloadBatchStatusCallback.class);
    private final DownloadBatchStatus percentageIncreasedStatus = anInternalDownloadsBatchStatus()
            .withPercentageDownloaded(DOWNLOAD_PERCENTAGE)
            .build();

    private final CallbackThrottleByProgressIncrease callbackThrottleByProgressIncrease = new CallbackThrottleByProgressIncrease();

    @Test
    public void doesNothing_whenCallbackUnset() {
        callbackThrottleByProgressIncrease.update(percentageIncreasedStatus);

        then(downloadBatchCallback).should(never()).onUpdate(percentageIncreasedStatus);
    }

    @Test
    public void doesNothing_whenDownloadBatchStatusIsUnchanged() {
        givenPreviousUpdate(percentageIncreasedStatus);

        callbackThrottleByProgressIncrease.update(percentageIncreasedStatus);
        then(downloadBatchCallback).should(never()).onUpdate(percentageIncreasedStatus);
    }

    @Test
    public void doesNotEmit_whenPercentageIsUnchanged() {
        callbackThrottleByProgressIncrease.setCallback(downloadBatchCallback);
        givenPreviousUpdate(percentageIncreasedStatus);

        callbackThrottleByProgressIncrease.update(percentageIncreasedStatus);

        then(downloadBatchCallback).should(never()).onUpdate(any(DownloadBatchStatus.class));
    }

    @Test
    public void doesNotEmitsStatus_whenStoppingUpdates() {
        callbackThrottleByProgressIncrease.setCallback(downloadBatchCallback);
        callbackThrottleByProgressIncrease.stopUpdates();

        callbackThrottleByProgressIncrease.update(percentageIncreasedStatus);

        then(downloadBatchCallback).should(never()).onUpdate(percentageIncreasedStatus);
    }

    private void givenPreviousUpdate(DownloadBatchStatus downloadBatchStatus) {
        callbackThrottleByProgressIncrease.update(downloadBatchStatus);
        reset(downloadBatchCallback);
    }
}
