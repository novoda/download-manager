package com.novoda.downloadmanager.lib;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class DownloadInfoTest {

    private static final DownloadClientReadyChecker IS_READY = new DownloadClientReadyChecker() {
        @Override
        public boolean isAllowedToDownload() {
            return true;
        }
    };

    private static final DownloadClientReadyChecker IS_NOT_READY = new DownloadClientReadyChecker() {
        @Override
        public boolean isAllowedToDownload() {
            return false;
        }
    };

    @Test
    public void givenClientIsReadyToDownloadThenStartDownload() {
        DownloadInfo downloadInfo = new DownloadInfo(
                null,
                null,
                null,
                null,
                null,
                IS_READY,
                null);

        boolean isReady = downloadInfo.isReadyToDownload();

        assertThat(isReady).isTrue();
    }

    @Test
    public void givenClientIsNotReadyToDownloadThenDownloadDoesNotStart() {
        DownloadInfo downloadInfo = new DownloadInfo(
                null,
                null,
                null,
                null,
                null,
                IS_NOT_READY,
                null);

        boolean isReady = downloadInfo.isReadyToDownload();

        assertThat(isReady).isFalse();
    }
}

