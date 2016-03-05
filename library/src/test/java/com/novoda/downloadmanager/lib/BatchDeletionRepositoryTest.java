package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BatchDeletionRepositoryTest {

    private static final String[] PROJECT_BATCH_ID = {DownloadContract.Batches._ID};
    private static final String WHERE_DELETED_VALUE_IS = DownloadContract.Batches.COLUMN_DELETED + " = ?";
    private static final String[] MARKED_FOR_DELETION = {"1"};
    private static final String _ID = "_id";
    private static final Uri BATCHES_URI = mock(Uri.class);
    private static final long BATCH_ID_1 = 1L;
    private static final long BATCH_ID_2 = 2L;
    private static final long BATCH_ID_3 = 3L;
    private static final long BATCH_ID_4 = 4L;

    @Mock
    private ContentResolver mockContentResolver;
    @Mock
    private DownloadDeleter mockDownloadDeleter;
    @Mock
    private FileDownloadInfo mockFileDownloadInfoId1;
    @Mock
    private FileDownloadInfo mockFileDownloadInfoId2;
    @Mock
    private FileDownloadInfo mockFileDownloadInfoId3;
    @Mock
    private FileDownloadInfo mockFileDownloadInfoId4;
    @Mock
    private DownloadsUriProvider mockDownloadsUriProvider;

    private BatchDeletionRepository batchDeletionRepository;

    @Before
    public void setUp() throws Exception {
        when(mockDownloadsUriProvider.getBatchesUri()).thenReturn(BATCHES_URI);
        when(mockFileDownloadInfoId1.getBatchId()).thenReturn(BATCH_ID_1);
        when(mockFileDownloadInfoId2.getBatchId()).thenReturn(BATCH_ID_2);
        when(mockFileDownloadInfoId3.getBatchId()).thenReturn(BATCH_ID_3);
        when(mockFileDownloadInfoId4.getBatchId()).thenReturn(BATCH_ID_4);

        batchDeletionRepository = new BatchDeletionRepository(mockDownloadDeleter, mockContentResolver, mockDownloadsUriProvider);
    }

    @Test
    public void givenThereAreFourBatchesMarkedToBeDeletedWhenDeletingMarkedBatchesForAllDownloadsThenItRemovesAll() {
        Cursor cursorWithDownloadsIdToBeDeleted = new MockCursorWithBatchIds(Arrays.asList(BATCH_ID_1, BATCH_ID_2, BATCH_ID_3, BATCH_ID_4));
        when(mockContentResolver.query(BATCHES_URI, PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null)).thenReturn(cursorWithDownloadsIdToBeDeleted);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), any(String.class), any(String[].class), any(String.class))).thenReturn(cursorWithDownloadsIdToBeDeleted);

        Collection<FileDownloadInfo> downloads = Arrays.asList(mockFileDownloadInfoId1, mockFileDownloadInfoId2, mockFileDownloadInfoId3, mockFileDownloadInfoId4);
        batchDeletionRepository.deleteMarkedBatchesFor(downloads);

        verify(mockContentResolver).query(BATCHES_URI, PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId1);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId2);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId3);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId4);
        verify(mockContentResolver).delete(BATCHES_URI, _ID + " IN (?, ?, ?, ?)", new String[]{"1", "2", "3", "4"});
    }

    @Test
    public void whenThereAreTwoBatchesMarkedToBeDeletedAndFourBatchesThenRemoveAll() {
        Cursor cursorWithDownloadsIdToBeDeleted = new MockCursorWithBatchIds(Arrays.asList(BATCH_ID_2, BATCH_ID_3));
        when(mockContentResolver.query(BATCHES_URI, PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null)).thenReturn(cursorWithDownloadsIdToBeDeleted);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), any(String.class), any(String[].class), any(String.class))).thenReturn(cursorWithDownloadsIdToBeDeleted);

        Collection<FileDownloadInfo> downloads = Arrays.asList(mockFileDownloadInfoId1, mockFileDownloadInfoId2, mockFileDownloadInfoId3, mockFileDownloadInfoId4);
        batchDeletionRepository.deleteMarkedBatchesFor(downloads);

        verify(mockContentResolver).query(BATCHES_URI, PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId1);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId2);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId3);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId4);
        verify(mockContentResolver).delete(BATCHES_URI, _ID + " IN (?, ?)", new String[]{"2", "3"});
    }

    @Test
    public void whenThereNoBatchesMarkedToBeDeletedAndFourBatchesThenRemoveAll() {
        Cursor cursorWithDownloadsIdToBeDeleted = new MockCursorWithBatchIds(Collections.<Long>emptyList());
        when(mockContentResolver.query(BATCHES_URI, PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null)).thenReturn(cursorWithDownloadsIdToBeDeleted);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), any(String.class), any(String[].class), any(String.class))).thenReturn(cursorWithDownloadsIdToBeDeleted);

        Collection<FileDownloadInfo> downloads = Arrays.asList(mockFileDownloadInfoId1, mockFileDownloadInfoId2, mockFileDownloadInfoId3, mockFileDownloadInfoId4);
        batchDeletionRepository.deleteMarkedBatchesFor(downloads);

        verify(mockContentResolver).query(BATCHES_URI, PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId1);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId2);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId3);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId4);
        verify(mockContentResolver, never()).delete(any(Uri.class), any(String.class), any(String[].class));
    }

}
