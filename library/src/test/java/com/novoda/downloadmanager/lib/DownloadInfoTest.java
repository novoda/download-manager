package com.novoda.downloadmanager.lib;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DownloadInfoTest {

    private static final DownloadClientReadyChecker IS_READY = new DownloadClientReadyChecker() {
        @Override
        public boolean isAllowedToDownload(Download download) {
            return true;
        }
    };

    private static final DownloadClientReadyChecker IS_NOT_READY = new DownloadClientReadyChecker() {
        @Override
        public boolean isAllowedToDownload(Download download) {
            return false;
        }
    };

    @Test
    public void givenClientIsReadyToDownloadThenStartDownload() {
        DownloadInfo downloadInfo = new DownloadInfo(
                null,
                null,
                null,
                IS_READY,
                null);

        boolean isReady = downloadInfo.isReadyToDownload(mock(DownloadBatch.class));

        assertThat(isReady).isTrue();
    }

    @Test
    public void givenClientIsNotReadyToDownloadThenDownloadDoesNotStart() {
        DownloadInfo downloadInfo = new DownloadInfo(
                null,
                null,
                null,
                IS_NOT_READY,
                null);

        boolean isReady = downloadInfo.isReadyToDownload(mock(DownloadBatch.class));

        assertThat(isReady).isFalse();
    }
}

