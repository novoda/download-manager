package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BatchDeletionRepositoryTest {

    private static final String[] PROJECT_BATCH_ID = {DownloadContract.Batches._ID};
    private static final String WHERE_DELETED_VALUE_IS = DownloadContract.Batches.COLUMN_DELETED + " = ?";
    private static final String[] MARKED_FOR_DELETION = {"1"};
    private static final String _ID = "_id";

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
        batchDeletionRepository = new BatchDeletionRepository(mockDownloadDeleter, mockContentResolver, mockDownloadsUriProvider);


    }

    @Test
    public void givenThereAreFourBatchesMarkedToBeDeletedWhenDeletingMarkedBatchesForAllDownloadsThenItRemovesAll() {
        Cursor cursorWithDownloadsIdToBeDeleted = new MockCursorWithBatchIds(Arrays.asList(1L, 2L, 3L, 4L));
        when(mockContentResolver.query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null)).thenReturn(cursorWithDownloadsIdToBeDeleted);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), any(String.class), any(String[].class), any(String.class))).thenReturn(cursorWithDownloadsIdToBeDeleted);

        Collection<FileDownloadInfo> downloads = Arrays.asList(mockFileDownloadInfoId1, mockFileDownloadInfoId2, mockFileDownloadInfoId3, mockFileDownloadInfoId4);
        batchDeletionRepository.deleteMarkedBatchesFor(downloads);

        verify(mockContentResolver).query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId1);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId2);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId3);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId4);
        verify(mockContentResolver).delete(this.mockDownloadsUriProvider.getBatchesUri(), _ID + " IN (?, ?, ?, ?)", new String[]{"1", "2", "3", "4"});
    }

    @Test
    public void whenThereAreTwoBatchesMarkedToBeDeletedAndFourBatchesThenRemoveAll() {
        Cursor cursorWithDownloadsIdToBeDeleted = new MockCursorWithBatchIds(Arrays.asList(2L, 3L));
        when(mockContentResolver.query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null)).thenReturn(cursorWithDownloadsIdToBeDeleted);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), any(String.class), any(String[].class), any(String.class))).thenReturn(cursorWithDownloadsIdToBeDeleted);

        Collection<FileDownloadInfo> downloads = Arrays.asList(mockFileDownloadInfoId1, mockFileDownloadInfoId2, mockFileDownloadInfoId3, mockFileDownloadInfoId4);
        batchDeletionRepository.deleteMarkedBatchesFor(downloads);

        verify(mockContentResolver).query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId1);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId2);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockFileDownloadInfoId3);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId4);
        verify(mockContentResolver).delete(this.mockDownloadsUriProvider.getBatchesUri(), _ID + " IN (?, ?)", new String[]{"2", "3"});
    }

    @Test
    public void whenThereNoBatchesMarkedToBeDeletedAndFourBatchesThenRemoveAll() {
        Cursor cursorWithDownloadsIdToBeDeleted = new MockCursorWithBatchIds(Collections.<Long>emptyList());
        when(mockContentResolver.query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null)).thenReturn(cursorWithDownloadsIdToBeDeleted);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), any(String.class), any(String[].class), any(String.class))).thenReturn(cursorWithDownloadsIdToBeDeleted);

        Collection<FileDownloadInfo> downloads = Arrays.asList(mockFileDownloadInfoId1, mockFileDownloadInfoId2, mockFileDownloadInfoId3, mockFileDownloadInfoId4);
        batchDeletionRepository.deleteMarkedBatchesFor(downloads);

        verify(mockContentResolver).query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId1);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId2);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId3);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockFileDownloadInfoId4);
        verify(mockContentResolver, never()).delete(any(Uri.class), any(String.class), any(String[].class));
    }

}
