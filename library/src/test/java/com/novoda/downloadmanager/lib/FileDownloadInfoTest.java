package com.novoda.downloadmanager.lib;

import com.novoda.downloadmanager.Download;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FileDownloadInfoTest {

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
        FileDownloadInfo fileDownloadInfo = new FileDownloadInfo(
                null,
                null,
                null,
                IS_READY,
                null,
                mock(PublicFacingDownloadMarshaller.class),
                canDownload);

        boolean isReady = fileDownloadInfo.isReadyToDownload(mock(DownloadBatch.class));

        assertThat(isReady).isTrue();
    }

    @Test
    public void givenClientIsNotReadyToDownloadThenDownloadDoesNotStart() {
        FileDownloadInfo fileDownloadInfo = new FileDownloadInfo(
                null,
                null,
                null,
                IS_NOT_READY,
                null,
                mock(PublicFacingDownloadMarshaller.class),
                canDownload);

        boolean isReady = fileDownloadInfo.isReadyToDownload(mock(DownloadBatch.class));

        assertThat(isReady).isFalse();
    }
}

