package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class BatchRepositoryTest {

    private static final String[] PROJECT_BATCH_ID = {Downloads.Impl.Batches._ID};
    private static final String WHERE_DELETED_VALUE_IS = Downloads.Impl.Batches.COLUMN_DELETED + " = ?";
    private static final String[] MARKED_FOR_DELETION = {"1"};
    private static final String _ID = "_id";

    @Mock
    private ContentResolver mockContentResolver;
    @Mock
    private DownloadDeleter mockDownloadDeleter;
    @Mock
    private FileDownloadInfo mockFileDownloadInfo;
    @Mock
    private FileDownloadInfo mockDownloadInfoId1;
    @Mock
    private FileDownloadInfo mockDownloadInfoId2;
    @Mock
    private FileDownloadInfo mockDownloadInfoId3;
    @Mock
    private FileDownloadInfo mockDownloadInfoId4;
    @Mock
    private DownloadsUriProvider mockDownloadsUriProvider;
    @Mock
    private Uri mockUri;

    private BatchRepository batchRepository;

    @Before
    public void setUp() {
        initMocks(this);

        when(mockDownloadsUriProvider.getBatchesUri()).thenReturn(mockUri);
        when(mockDownloadsUriProvider.getAllDownloadsUri()).thenReturn(mockUri);

        this.batchRepository = new BatchRepository(mockContentResolver, mockDownloadDeleter, mockDownloadsUriProvider);

        when(mockDownloadInfoId1.getBatchId()).thenReturn(1L);
        when(mockDownloadInfoId2.getBatchId()).thenReturn(2L);
        when(mockDownloadInfoId3.getBatchId()).thenReturn(3L);
        when(mockDownloadInfoId4.getBatchId()).thenReturn(4L);
    }

    @Test
    public void givenADownloadInfoWhenRetrievingTheBatchThenTheBatchIdsMatch() {
        long expectedBatchId = 100L;
        when(mockFileDownloadInfo.getBatchId()).thenReturn(expectedBatchId);
        Cursor batchCursor = new MockCursorWithBatchIds(Arrays.asList(expectedBatchId));
        when(mockContentResolver.query(any(Uri.class), any(String[].class), anyString(), any(String[].class), anyString())).thenReturn(batchCursor);

        DownloadBatch downloadBatch = batchRepository.retrieveBatchFor(mockFileDownloadInfo);

        assertThat(downloadBatch.getBatchId()).isEqualTo(expectedBatchId);
    }

    @Test
    public void givenADownloadInfoAndNoLinkedBatchesWhenRetrievingTheBatchThenTheBatchIsDeleted() {
        long batchIdToBeMissing = 100L;
        when(mockFileDownloadInfo.getBatchId()).thenReturn(batchIdToBeMissing);
        Cursor emptyBatchCursor = mock(Cursor.class);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), anyString(), any(String[].class), anyString())).thenReturn(emptyBatchCursor);

        DownloadBatch downloadBatch = batchRepository.retrieveBatchFor(mockFileDownloadInfo);

        assertThat(downloadBatch).isEqualTo(DownloadBatch.DELETED);
    }

    @Test
    public void whenThereAreFourBatchesMarkedToBeDeletedAndFourBatchesThenRemoveAll() {
        Cursor cursorWithDownloadsIdToBeDeleted = new MockCursorWithBatchIds(Arrays.asList(1L, 2L, 3L, 4L));
        when(mockContentResolver.query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null)).thenReturn(cursorWithDownloadsIdToBeDeleted);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), any(String.class), any(String[].class), any(String.class))).thenReturn(cursorWithDownloadsIdToBeDeleted);

        Collection<FileDownloadInfo> downloads = Arrays.asList(mockDownloadInfoId1, mockDownloadInfoId2, mockDownloadInfoId3, mockDownloadInfoId4);
        batchRepository.deleteMarkedBatchesFor(downloads);

        verify(mockContentResolver).query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockDownloadInfoId1);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockDownloadInfoId2);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockDownloadInfoId3);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockDownloadInfoId4);
        verify(mockContentResolver).delete(this.mockDownloadsUriProvider.getBatchesUri(), _ID + " IN (?)", new String[]{"1, 2, 3, 4"});
    }

    @Test
    public void whenThereAreTwoBatchesMarkedToBeDeletedAndFourBatchesThenRemoveAll() {
        Cursor cursorWithDownloadsIdToBeDeleted = new MockCursorWithBatchIds(Arrays.asList(2L, 3L));
        when(mockContentResolver.query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null)).thenReturn(cursorWithDownloadsIdToBeDeleted);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), any(String.class), any(String[].class), any(String.class))).thenReturn(cursorWithDownloadsIdToBeDeleted);

        Collection<FileDownloadInfo> downloads = Arrays.asList(mockDownloadInfoId1, mockDownloadInfoId2, mockDownloadInfoId3, mockDownloadInfoId4);
        batchRepository.deleteMarkedBatchesFor(downloads);

        verify(mockContentResolver).query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockDownloadInfoId1);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockDownloadInfoId2);
        verify(mockDownloadDeleter).deleteFileAndDatabaseRow(mockDownloadInfoId3);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockDownloadInfoId4);
        verify(mockContentResolver).delete(this.mockDownloadsUriProvider.getBatchesUri(), _ID + " IN (?)", new String[]{"2, 3"});
    }

    @Test
    public void whenThereNoBatchesMarkedToBeDeletedAndFourBatchesThenRemoveAll() {
        Cursor cursorWithDownloadsIdToBeDeleted = new MockCursorWithBatchIds(Collections.<Long>emptyList());
        when(mockContentResolver.query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null)).thenReturn(cursorWithDownloadsIdToBeDeleted);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), any(String.class), any(String[].class), any(String.class))).thenReturn(cursorWithDownloadsIdToBeDeleted);

        Collection<FileDownloadInfo> downloads = Arrays.asList(mockDownloadInfoId1, mockDownloadInfoId2, mockDownloadInfoId3, mockDownloadInfoId4);
        batchRepository.deleteMarkedBatchesFor(downloads);

        verify(mockContentResolver).query(mockDownloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockDownloadInfoId1);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockDownloadInfoId2);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockDownloadInfoId3);
        verify(mockDownloadDeleter, never()).deleteFileAndDatabaseRow(mockDownloadInfoId4);
        verify(mockContentResolver, never()).delete(any(Uri.class), any(String.class), any(String[].class));
    }

    private static class MockCursorWithBatchIds implements Cursor {

        private final List<Long> ids;
        private int position = -1;

        public MockCursorWithBatchIds(List<Long> ids) {
            this.ids = ids;
        }

        @Override
        public int getCount() {
            return ids.size();
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public boolean move(int i) {
            return position + i >= 0 && position + i < ids.size();
        }

        @Override
        public boolean moveToPosition(int i) {
            return i >= 0 && i < ids.size();
        }

        @Override
        public boolean moveToFirst() {
            position = 0;
            return true;
        }

        @Override
        public boolean moveToLast() {
            position = ids.size() - 1;
            return false;
        }

        @Override
        public boolean moveToNext() {
            if (position < ids.size() - 1) {
                position++;
                return true;
            }
            return false;
        }

        @Override
        public boolean moveToPrevious() {
            if (position > 1) {
                position--;
                return true;
            }
            return false;
        }

        @Override
        public boolean isFirst() {
            return position == 0;
        }

        @Override
        public boolean isLast() {
            return position == ids.size() - 1;
        }

        @Override
        public boolean isBeforeFirst() {
            return false;
        }

        @Override
        public boolean isAfterLast() {
            return false;
        }

        @Override
        public int getColumnIndex(String s) {
            return 0;
        }

        @Override
        public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
            return 0;
        }

        @Override
        public String getColumnName(int i) {
            return null;
        }

        @Override
        public String[] getColumnNames() {
            return new String[0];
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public byte[] getBlob(int i) {
            return new byte[0];
        }

        @Override
        public String getString(int i) {
            return null;
        }

        @Override
        public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {

        }

        @Override
        public short getShort(int i) {
            return 0;
        }

        @Override
        public int getInt(int i) {
            return 0;
        }

        @Override
        public long getLong(int i) {
            return ids.get(position);
        }

        @Override
        public float getFloat(int i) {
            return 0;
        }

        @Override
        public double getDouble(int i) {
            return 0;
        }

        @Override
        public int getType(int i) {
            return 0;
        }

        @Override
        public boolean isNull(int i) {
            return ids == null;
        }

        @Override
        public void deactivate() {

        }

        @Override
        public boolean requery() {
            return false;
        }

        @Override
        public void close() {

        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public void registerContentObserver(ContentObserver contentObserver) {

        }

        @Override
        public void unregisterContentObserver(ContentObserver contentObserver) {

        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void setNotificationUri(ContentResolver contentResolver, Uri uri) {

        }

        @Override
        public Uri getNotificationUri() {
            return null;
        }

        @Override
        public boolean getWantsAllOnMoveCalls() {
            return false;
        }

        @Override
        public Bundle getExtras() {
            return null;
        }

        @Override
        public Bundle respond(Bundle bundle) {
            return null;
        }
    }
}
