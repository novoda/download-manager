package com.novoda.downloadmanager;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.novoda.downloadmanager.InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus;

public class DownloadBatchStatusFilterTest {

    private final FileDownloader.FileDownloadError firstError = FileDownloader.FileDownloadError.createFrom(
            "www.example.com",
            "first",
            -1
    );

    private final FileDownloader.FileDownloadError secondError = FileDownloader.FileDownloadError.createFrom(
            "www.example.com",
            "second",
            -1
    );
    private final InternalDownloadBatchStatus firstPercentageStatus = anInternalDownloadsBatchStatus()
            .withPercentageDownloaded(75)
            .build();
    private final InternalDownloadBatchStatus secondPercentageStatus = anInternalDownloadsBatchStatus()
            .withPercentageDownloaded(80)
            .build();
    private final InternalDownloadBatchStatus firstErrorStatus = anInternalDownloadsBatchStatus()
            .withDownloadError(DownloadErrorFactory.createNetworkError(firstError))
            .build();
    private final InternalDownloadBatchStatus secondErrorStatus = anInternalDownloadsBatchStatus()
            .withDownloadError(DownloadErrorFactory.createNetworkError(secondError))
            .build();
    private final InternalDownloadBatchStatus firstStatus = anInternalDownloadsBatchStatus()
            .build();
    private final InternalDownloadBatchStatus secondStatus = anInternalDownloadsBatchStatus()
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

    private void givenPreviousUpdate(InternalDownloadBatchStatus downloadBatchStatus) {
        downloadBatchStatusFilter.shouldFilterOut(downloadBatchStatus);
    }
}
