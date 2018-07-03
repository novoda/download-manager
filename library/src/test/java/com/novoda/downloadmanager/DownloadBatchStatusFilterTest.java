package com.novoda.downloadmanager;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.novoda.downloadmanager.InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

public class DownloadBatchStatusFilterTest {

    private final DownloadBatchStatusCallback downloadBatchCallback = mock(DownloadBatchStatusCallback.class);
    private final DownloadBatchStatus firstPercentageStatus = anInternalDownloadsBatchStatus()
            .withPercentageDownloaded(75)
            .build();
    private final DownloadBatchStatus secondPercentageStatus = anInternalDownloadsBatchStatus()
            .withPercentageDownloaded(80)
            .build();
    private final DownloadBatchStatus firstErrorStatus = anInternalDownloadsBatchStatus()
            .withDownloadError(DownloadErrorFactory.createNetworkError("first"))
            .build();
    private final DownloadBatchStatus secondErrorStatus = anInternalDownloadsBatchStatus()
            .withDownloadError(DownloadErrorFactory.createNetworkError("second"))
            .build();
    private final DownloadBatchStatus firstStatus = anInternalDownloadsBatchStatus()
            .build();
    private final DownloadBatchStatus secondStatus = anInternalDownloadsBatchStatus()
            .withStatus(DownloadBatchStatus.Status.DOWNLOADED)
            .build();

    private final DownloadBatchStatusFilter downloadBatchStatusFilter = new DownloadBatchStatusFilter();

    @Test
    public void returnsFalse_whenPercentageDoesNotMatchPrevious() {
        givenPreviousUpdate(firstPercentageStatus);

        boolean shouldFilterOut = downloadBatchStatusFilter.shouldFilterOut(secondPercentageStatus);

        assertThat(shouldFilterOut).isFalse();
    }

    @Test
    public void returnsTrue_whenPercentageIsUnchanged() {
        givenPreviousUpdate(firstPercentageStatus);

        boolean shouldFilterOut = downloadBatchStatusFilter.shouldFilterOut(firstPercentageStatus);

        assertThat(shouldFilterOut).isTrue();
    }

    @Test
    public void returnsFalse_whenErrorDoesNotMatchPrevious() {
        givenPreviousUpdate(firstErrorStatus);

        boolean shouldFilterOut = downloadBatchStatusFilter.shouldFilterOut(secondErrorStatus);

        assertThat(shouldFilterOut).isFalse();
    }

    @Test
    public void returnsTrue_whenErrorIsUnchanged() {
        givenPreviousUpdate(firstErrorStatus);

        boolean shouldFilterOut = downloadBatchStatusFilter.shouldFilterOut(firstErrorStatus);

        assertThat(shouldFilterOut).isTrue();
    }

    @Test
    public void returnsFalse_whenStatusDoesNotMatchPrevious() {
        givenPreviousUpdate(firstStatus);

        boolean shouldFilterOut = downloadBatchStatusFilter.shouldFilterOut(secondStatus);

        assertThat(shouldFilterOut).isFalse();
    }

    @Test
    public void returnsTrue_whenStatusIsUnchanged() {
        givenPreviousUpdate(firstStatus);

        boolean shouldFilterOut = downloadBatchStatusFilter.shouldFilterOut(firstStatus);

        assertThat(shouldFilterOut).isTrue();
    }

    private void givenPreviousUpdate(DownloadBatchStatus downloadBatchStatus) {
        downloadBatchStatusFilter.shouldFilterOut(downloadBatchStatus);
        reset(downloadBatchCallback);
    }
}
