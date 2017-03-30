package com.novoda.downloadmanager.service;

import com.novoda.downloadmanager.Pauser;
import com.novoda.downloadmanager.client.DownloadCheck;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.download.DownloadHandler;
import com.novoda.notils.logger.simple.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

public class DownloadUpdaterTest {

    private static final DownloadFile DOWNLOAD_FILE = DownloadFileFixtures.aDownloadFile().build();
    private static final DownloadFile UNKNOWN_SIZE_DOWNLOAD_FILE = DownloadFileFixtures.aDownloadFile().withTotalSize(-1).build();
    private static final DownloadFile ANOTHER_UNKNOWN_SIZE_DOWNLOAD_FILE = DownloadFileFixtures.aDownloadFile().withTotalSize(-1).build();

    private static final List<Download> DOWNLOADS = Arrays.asList(
            DownloadFixtures.aDownload()
                    .withDownloadFiles(
                            Arrays.asList(DOWNLOAD_FILE, UNKNOWN_SIZE_DOWNLOAD_FILE)
                    ).build(),
            DownloadFixtures.aDownload()
                    .withDownloadId(new DownloadId(67890))
                    .withDownloadFiles(Arrays.asList(
                            DOWNLOAD_FILE, ANOTHER_UNKNOWN_SIZE_DOWNLOAD_FILE
                    ))
                    .build()
    );

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    DownloadHandler downloadHandler;
    @Mock
    ExecutorService executorService;
    @Mock
    Pauser pauser;
    @Mock
    DownloadCheck downloadCheck;

    private DownloadUpdater downloadUpdater;

    @Before
    public void setUp() {
        downloadUpdater = new DownloadUpdater(downloadHandler, executorService, pauser, downloadCheck);
        Log.setShowLogs(true);
    }

    @Test
    public void whenReleasing_thenShutsdownExecutor() {

        downloadUpdater.release();

        verify(executorService).shutdown();
    }

    @Test
    public void givenDownloads_whenUpdating_thenDeletesMarkedBatchesForDownloads() {
        given(downloadHandler.getAllDownloads()).willReturn(DOWNLOADS);

        downloadUpdater.update();

        verify(downloadHandler).deleteMarkedBatchesFor(DOWNLOADS);
    }

    @Test
    public void givenDownloads_withFiles_withUnknownSizes_whenUpdating_thenUpdatesFileSizeForFilesWithUnknownSize() {
        given(downloadHandler.getAllDownloads()).willReturn(DOWNLOADS);

        downloadUpdater.update();

        InOrder inOrder = inOrder(downloadHandler);
        inOrder.verify(downloadHandler).updateFileSize(UNKNOWN_SIZE_DOWNLOAD_FILE);
        inOrder.verify(downloadHandler).updateFileSize(ANOTHER_UNKNOWN_SIZE_DOWNLOAD_FILE);
    }
}
