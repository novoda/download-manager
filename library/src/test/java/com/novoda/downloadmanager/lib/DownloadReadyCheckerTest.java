package com.novoda.downloadmanager.lib;

import com.novoda.downloadmanager.Download;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DownloadReadyCheckerTest {

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
        DownloadReadyChecker downloadReadyChecker = new DownloadReadyChecker(
                null,
                null,
                IS_READY,
                mock(PublicFacingDownloadMarshaller.class),
                restartTimeCreator);

        boolean isReady = downloadReadyChecker.canDownload(mock(DownloadBatch.class));

        assertThat(isReady).isTrue();
    }

    @Test
    public void givenClientIsNotReadyToDownloadThenDownloadDoesNotStart() {
        DownloadReadyChecker downloadReadyChecker = new DownloadReadyChecker(
                null,
                null,
                IS_NOT_READY,
                mock(PublicFacingDownloadMarshaller.class),
                restartTimeCreator);

        boolean isReady = downloadReadyChecker.canDownload(mock(DownloadBatch.class));

        assertThat(isReady).isFalse();
    }
}

