package com.novoda.downloadmanager;

import android.os.Handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LiteDownloadManagerDownloaderTest {

    private final Object waitForDownloadService = new Object();
    private final Object waitForDownloadBatchStatusCallback = new Object();
    private final ExecutorService executor = mock(ExecutorService.class);
    private final Handler callbackHandler = mock(Handler.class);
    private final FileOperations fileOperations = mock(FileOperations.class);
    private final DownloadsBatchPersistence downloadsBatchPersistence = mock(DownloadsBatchPersistence.class);
    private final DownloadsFilePersistence downloadsFilePersistence = mock(DownloadsFilePersistence.class);
    private final DownloadBatchStatusNotificationDispatcher notificationDispatcher = mock(DownloadBatchStatusNotificationDispatcher.class);
    private final DownloadBatchRequirementRule downloadBatchRequirementRule = mock(DownloadBatchRequirementRule.class);
    private final ConnectionChecker connectionChecker = mock(ConnectionChecker.class);
    private final Set<DownloadBatchStatusCallback> callbacks = new HashSet<>();
    private final CallbackThrottleCreator callbackThrottleCreator = mock(CallbackThrottleCreator.class);
    private final DownloadBatchStatusFilter downloadBatchStatusFilter = mock(DownloadBatchStatusFilter.class);
    private final Wait.Criteria serviceCriteria = mock(Wait.Criteria.class);
    private final DownloadService downloadService = mock(DownloadService.class);

    private final DownloadBatch downloadBatch = mock(DownloadBatch.class, Mockito.RETURNS_DEEP_STUBS);
    private final DownloadBatch anotherDownloadBatchWithTheSameId = mock(DownloadBatch.class, Mockito.RETURNS_DEEP_STUBS);
    private final DownloadBatchId downloadBatchId = mock(DownloadBatchId.class);

    private final Map<DownloadBatchId, DownloadBatch> downloadingBatches = new HashMap<>();

    private LiteDownloadManagerDownloader downloader;

    @Before
    public void setUp() {
        setUpExecutorService();
        setUpBatches();

        downloader = new LiteDownloadManagerDownloader(
                waitForDownloadService,
                waitForDownloadBatchStatusCallback,
                executor,
                callbackHandler,
                fileOperations,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                notificationDispatcher,
                downloadBatchRequirementRule,
                connectionChecker,
                callbacks,
                callbackThrottleCreator,
                downloadBatchStatusFilter,
                serviceCriteria
        );

        downloader.setDownloadService(downloadService);
    }

    private void setUpExecutorService() {
        willAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).given(executor).submit(any(Runnable.class));
    }

    private void setUpBatches() {
        given(downloadBatch.getId()).willReturn(downloadBatchId);
        given(anotherDownloadBatchWithTheSameId.getId()).willReturn(downloadBatchId);
    }

    @Test
    public void addsDownloadBatchToQueue() {
        downloadingBatches.clear();

        downloader.download(downloadBatch, downloadingBatches);

        assertThat(downloadingBatches).containsEntry(downloadBatchId, downloadBatch);
    }

    @Test
    public void doesNotAddDownloadBatchToQueue_whenIdAlreadyExists() {
        downloadingBatches.put(downloadBatchId, anotherDownloadBatchWithTheSameId);

        downloader.download(downloadBatch, downloadingBatches);

        assertThat(downloadingBatches).doesNotContainEntry(downloadBatchId, downloadBatch);
    }

    @Test
    public void downloadsBatch() {
        downloadingBatches.clear();

        downloader.download(downloadBatch, downloadingBatches);

        verify(downloadService).download(eq(downloadBatch), any());
    }

    @Test
    public void downloadsBatchByOriginalReference_whenIdAlreadyExists() {
        downloadingBatches.put(downloadBatchId, anotherDownloadBatchWithTheSameId);

        downloader.download(downloadBatch, downloadingBatches);

        verify(downloadService).download(eq(anotherDownloadBatchWithTheSameId), any());
    }
}
