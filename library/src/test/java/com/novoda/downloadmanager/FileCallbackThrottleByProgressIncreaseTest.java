package com.novoda.downloadmanager;

import org.junit.Test;

import static com.novoda.downloadmanager.InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;

public class FileCallbackThrottleByProgressIncreaseTest {

    private static final int DOWNLOAD_PERCENTAGE = 75;

    private final DownloadBatchStatusCallback downloadBatchCallback = mock(DownloadBatchStatusCallback.class);
    private final DownloadBatchStatus percentageIncreasedStatus = anInternalDownloadsBatchStatus()
            .withPercentageDownloaded(DOWNLOAD_PERCENTAGE)
            .build();

    private final FileCallbackThrottleByProgressIncrease callbackThrottleByProgressIncrease = new FileCallbackThrottleByProgressIncrease();

    @Test(expected = IllegalStateException.class)
    public void doesNothing_whenCallbackUnset() {
        callbackThrottleByProgressIncrease.update(percentageIncreasedStatus);
    }

    @Test
    public void doesNothing_whenDownloadBatchStatusIsUnchanged() {
        callbackThrottleByProgressIncrease.setCallback(downloadBatchCallback);
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

    private void givenPreviousUpdate(DownloadBatchStatus downloadBatchStatus) {
        callbackThrottleByProgressIncrease.update(downloadBatchStatus);
        reset(downloadBatchCallback);
    }
}
