package com.novoda.downloadmanager.lib;

import android.database.Cursor;
import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BatchRepositoryTest {

    private static final long ANY_BATCH_ID = 1;
    private static final int ANY_STATUS = 2;
    private static final long ANY_DOWNLOAD_ID = 3;
    public static final FileDownloadInfo ANY_FILE_DOWNLOAD_INFO = mock(FileDownloadInfo.class);
    private static final BatchQuery ANY_BATCH_QUERY = new BatchQuery("any-selection", null, "any-sort-order");

    @Mock
    private BatchStatusService batchStatusService;
    @Mock
    private BatchStartingService batchStartingService;
    @Mock
    private BatchDeletionService batchDeletionService;
    @Mock
    private BatchRetrievalService batchRetrievalService;

    private BatchRepository batchRepository;

    @Before
    public void setUp() throws Exception {
        batchRepository = new BatchRepository(batchStatusService, batchStartingService, batchDeletionService, batchRetrievalService);
    }

    @Test
    public void testUpdateBatchStatus() throws Exception {
        batchRepository.updateBatchStatus(ANY_BATCH_ID, ANY_STATUS);

        verify(batchStatusService).updateBatchStatus(ANY_BATCH_ID, ANY_STATUS);
    }

    @Test
    public void testGetBatchStatus() throws Exception {
        when(batchStatusService.getBatchStatus(ANY_BATCH_ID)).thenReturn(ANY_STATUS);

        int status = batchRepository.getBatchStatus(ANY_BATCH_ID);

        assertThat(status).isEqualTo(ANY_STATUS);
    }

    @Test
    public void testCalculateBatchStatus() throws Exception {
        when(batchStatusService.calculateBatchStatusFromDownloads(ANY_BATCH_ID)).thenReturn(ANY_STATUS);

        int status = batchRepository.calculateBatchStatus(ANY_BATCH_ID);

        assertThat(status).isEqualTo(ANY_STATUS);
    }

    @Test
    public void testSetBatchItemsCancelled() throws Exception {
        batchRepository.setBatchItemsCancelled(ANY_BATCH_ID);

        verify(batchStatusService).setBatchItemsCancelled(ANY_BATCH_ID);
    }

    @Test
    public void testCancelBatch() throws Exception {
        batchRepository.cancelBatch(ANY_BATCH_ID);

        verify(batchStatusService).cancelBatch(ANY_BATCH_ID);
    }

    @Test
    public void testSetBatchItemsFailed() throws Exception {
        batchRepository.setBatchItemsFailed(ANY_BATCH_ID, ANY_DOWNLOAD_ID);

        verify(batchStatusService).setBatchItemsFailed(ANY_BATCH_ID, ANY_DOWNLOAD_ID);
    }

    @Test
    public void testUpdateBatchesToPendingStatus() throws Exception {
        List<String> batchIdsToBeUnlocked = Collections.singletonList(String.valueOf(ANY_BATCH_ID));
        int expectedModifiedCount = 1;
        when(batchStatusService.updateBatchToPendingStatus(batchIdsToBeUnlocked)).thenReturn(expectedModifiedCount);

        int modified = batchRepository.updateBatchesToPendingStatus(batchIdsToBeUnlocked);

        verify(batchStatusService).updateBatchToPendingStatus(batchIdsToBeUnlocked);
        assertThat(modified).isEqualTo(expectedModifiedCount);
    }

    @Test
    public void testIsBatchStartingForTheFirstTime() throws Exception {
        boolean batchIsStartingForFirstTime = true;
        when(batchStartingService.isBatchStartingForTheFirstTime(ANY_BATCH_ID)).thenReturn(batchIsStartingForFirstTime);

        boolean isStartingForTheFirstTime = batchRepository.isBatchStartingForTheFirstTime(ANY_BATCH_ID);

        assertThat(isStartingForTheFirstTime).isEqualTo(batchIsStartingForFirstTime);
    }

    @Test
    public void testMarkBatchAsStarted() throws Exception {
        batchRepository.markBatchAsStarted(ANY_BATCH_ID);

        verify(batchStartingService).markBatchAsStarted(ANY_BATCH_ID);
    }

    @Test
    public void testRetrieveBatchFor() throws Exception {
        DownloadBatch expectedDownloadBatch = mock(DownloadBatch.class);
        when(batchRetrievalService.retrieveBatchFor(ANY_FILE_DOWNLOAD_INFO)).thenReturn(expectedDownloadBatch);

        DownloadBatch downloadBatch = batchRepository.retrieveBatchFor(ANY_FILE_DOWNLOAD_INFO);

        assertThat(downloadBatch).isEqualTo(expectedDownloadBatch);
    }

    @Test
    public void testRetrieveBatchesFor() throws Exception {
        List<FileDownloadInfo> downloads = givenDownloads();
        List<DownloadBatch> expectedBatches = Collections.singletonList(mock(DownloadBatch.class));
        when(batchRetrievalService.retrieveBatchesFor(downloads)).thenReturn(expectedBatches);

        List<DownloadBatch> batches = batchRepository.retrieveBatchesFor(downloads);

        assertThat(batches).isEqualTo(expectedBatches);
    }

    @Test
    public void testRetrieveFor() throws Exception {
        Cursor expectedCursor = mock(Cursor.class);
        when(batchRetrievalService.retrieveFor(ANY_BATCH_QUERY)).thenReturn(expectedCursor);

        Cursor cursor = batchRepository.retrieveFor(ANY_BATCH_QUERY);

        assertThat(cursor).isEqualTo(expectedCursor);
    }

    @Test
    public void testDeleteMarkedBatchesFor() throws Exception {
        List<FileDownloadInfo> downloads = givenDownloads();
        batchRepository.deleteMarkedBatchesFor(downloads);

        verify(batchDeletionService).deleteMarkedBatchesFor(downloads);
    }

    @NonNull
    private List<FileDownloadInfo> givenDownloads() {
        return Collections.singletonList(ANY_FILE_DOWNLOAD_INFO);
    }
}
