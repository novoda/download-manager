package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.google.common.truth.Truth.assertThat;
import static com.novoda.downloadmanager.DownloadBatchIdFixtures.aDownloadBatchId;
import static com.novoda.downloadmanager.InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class DownloadManagerTest {

    private static final DownloadBatchId DOWNLOAD_BATCH_ID = aDownloadBatchId().withRawDownloadBatchId("id01").build();
    private static final DownloadBatchId ADDITIONAL_DOWNLOAD_BATCH_ID = aDownloadBatchId().withRawDownloadBatchId("id02").build();
    private static final Batch BATCH = new Batch.Builder(DOWNLOAD_BATCH_ID, "title").build();

    private final AllStoredDownloadsSubmittedCallback allStoredDownloadsSubmittedCallback = mock(AllStoredDownloadsSubmittedCallback.class);
    private final AllBatchStatusesCallback allBatchStatusesCallback = mock(AllBatchStatusesCallback.class);
    private final DownloadService downloadService = mock(DownloadService.class);
    private final Object lock = mock(Object.class);
    private final DownloadBatch downloadBatch = mock(DownloadBatch.class);
    private final DownloadBatch additionalDownloadBatch = mock(DownloadBatch.class);
    private final ExecutorService executorService = mock(ExecutorService.class);
    private final DownloadBatchCallback downloadBatchCallback = mock(DownloadBatchCallback.class);
    private final FileOperations fileOperations = mock(FileOperations.class);
    private final DownloadsBatchPersistence downloadsBatchPersistence = mock(DownloadsBatchPersistence.class);
    private final LiteDownloadManagerDownloader downloadManagerDownloader = mock(LiteDownloadManagerDownloader.class);
    private List<DownloadBatchCallback> downloadBatchCallbacks = new ArrayList<>();

    private DownloadManager downloadManager;
    private Map<DownloadBatchId, DownloadBatch> downloadBatches;

    @Before
    public void setUp() {
        downloadBatches = new HashMap<>();
        downloadBatches.put(DOWNLOAD_BATCH_ID, downloadBatch);
        downloadBatches.put(ADDITIONAL_DOWNLOAD_BATCH_ID, additionalDownloadBatch);

        downloadBatchCallbacks.add(downloadBatchCallback);

        downloadManager = new DownloadManager(
                lock,
                executorService,
                downloadBatches,
                downloadBatchCallbacks,
                fileOperations,
                downloadsBatchPersistence,
                downloadManagerDownloader
        );

        final ArgumentCaptor<DownloadsBatchPersistence.LoadBatchesCallback> loadBatchesCallbackCaptor = ArgumentCaptor.forClass(DownloadsBatchPersistence.LoadBatchesCallback.class);
        willAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                loadBatchesCallbackCaptor.getValue().onLoaded(Arrays.asList(downloadBatch, additionalDownloadBatch));
                return null;
            }
        }).given(downloadsBatchPersistence).loadAsync(any(FileOperations.class), loadBatchesCallbackCaptor.capture());
    }

    @Test
    public void setDownloadService_whenInitialising() {
        downloadManager.initialise(downloadService);

        verify(downloadManagerDownloader).setDownloadService(downloadService);
    }

    @Ignore // How do we unit test the synchronized block?
    @Test
    public void notifyAll_whenInitialising() {
        downloadManager.initialise(downloadService);

        verify(lock).notifyAll();
    }

    @Test
    public void triggersDownloadOfBatches_whenSubmittingAllStoredDownloads() {
        downloadManager.submitAllStoredDownloads(allStoredDownloadsSubmittedCallback);

        InOrder inOrder = inOrder(downloadManagerDownloader);
        inOrder.verify(downloadManagerDownloader).download(downloadBatch, downloadBatches);
        inOrder.verify(downloadManagerDownloader).download(additionalDownloadBatch, downloadBatches);
    }

    @Test
    public void notifies_whenSubmittingAllStoredDownloads() {
        downloadManager.submitAllStoredDownloads(allStoredDownloadsSubmittedCallback);

        verify(allStoredDownloadsSubmittedCallback).onAllDownloadsSubmitted();
    }

    @Test
    public void downloadGivenBatch() {
        downloadManager.download(BATCH);

        verify(downloadManagerDownloader).download(BATCH, downloadBatches);
    }

    @Test
    public void doesNotPause_whenBatchIdIsUnknown() {
        downloadManager.pause(new LiteDownloadBatchId("unknown"));

        verifyZeroInteractions(downloadBatch, additionalDownloadBatch);
    }

    @Test
    public void pausesBatch() {
        downloadManager.pause(DOWNLOAD_BATCH_ID);

        verify(downloadBatch).pause();
    }

    @Test
    public void doesNotResume_whenBatchIdIsUnknown() {
        downloadManager.pause(new LiteDownloadBatchId("unknown"));

        verifyZeroInteractions(downloadBatch, additionalDownloadBatch);
    }

    @Test
    public void doesNotResume_whenBatchIsAlreadyDownloading() {
        given(downloadBatch.status()).willReturn(anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.DOWNLOADING).build());

        downloadManager.resume(DOWNLOAD_BATCH_ID);

        InOrder inOrder = inOrder(downloadBatch);
        inOrder.verify(downloadBatch).status();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void removesBatchFromInternalList_whenResuming() {
        given(downloadBatch.status()).willReturn(anInternalDownloadsBatchStatus().build());

        downloadManager.resume(DOWNLOAD_BATCH_ID);

        assertThat(downloadBatches).doesNotContainEntry(DOWNLOAD_BATCH_ID, downloadBatch);
    }

    @Test
    public void resumesBatch() {
        given(downloadBatch.status()).willReturn(anInternalDownloadsBatchStatus().build());

        downloadManager.resume(DOWNLOAD_BATCH_ID);

        verify(downloadBatch).resume();
    }

    @Test
    public void triggersDownload_whenResumingBatch() {
        given(downloadBatch.status()).willReturn(anInternalDownloadsBatchStatus().build());

        downloadManager.resume(DOWNLOAD_BATCH_ID);

        verify(downloadManagerDownloader).download(downloadBatch, downloadBatches);
    }

    @Test
    public void doesNotDelete_whenBatchIdIsUnknown() {
        downloadManager.delete(new LiteDownloadBatchId("unknown"));

        verifyZeroInteractions(downloadBatch, additionalDownloadBatch);
    }

    @Test
    public void removesBatchFromInternalList_whenDeleting() {
        downloadManager.delete(DOWNLOAD_BATCH_ID);

        assertThat(downloadBatches).doesNotContainEntry(DOWNLOAD_BATCH_ID, downloadBatch);
    }

    @Test
    public void deletesBatch() {
        downloadManager.delete(DOWNLOAD_BATCH_ID);

        verify(downloadBatch).delete();
    }

    @Test
    public void addsCallbackToInternalList() {
        DownloadBatchCallback additionalDownloadBatchCallback = mock(DownloadBatchCallback.class);

        downloadManager.addDownloadBatchCallback(additionalDownloadBatchCallback);

        assertThat(downloadBatchCallbacks).contains(additionalDownloadBatchCallback);
    }

    @Test
    public void removesCallbackFromInternalList() {
        downloadManager.removeDownloadBatchCallback(downloadBatchCallback);

        assertThat(downloadBatchCallbacks).doesNotContain(downloadBatchCallback);
    }

    @Ignore // How to test the synchronized block here?
    @Test
    public void waitsForServiceToExist_whenGettingAllBatchStatuses() {
        downloadManager.getAllDownloadBatchStatuses(allBatchStatusesCallback);
    }

    @Test
    public void getsAllBatchStatuses_whenServiceAlreadyExists() {
        InternalDownloadBatchStatus status = anInternalDownloadsBatchStatus().build();
        given(downloadBatch.status()).willReturn(status);
        InternalDownloadBatchStatus additionalStatus = anInternalDownloadsBatchStatus().build();
        given(additionalDownloadBatch.status()).willReturn(additionalStatus);
        downloadManager.initialise(mock(DownloadService.class));

        downloadManager.getAllDownloadBatchStatuses(allBatchStatusesCallback);

        ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
        
        verify(allBatchStatusesCallback).onReceived(argumentCaptor.capture());
        List<DownloadBatchStatus> actualStatuses = argumentCaptor.getValue();
        assertThat(actualStatuses).containsAllOf(status, additionalStatus);
    }

}
