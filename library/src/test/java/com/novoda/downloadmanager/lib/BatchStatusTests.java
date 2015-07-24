package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BatchStatusTests {

    @Test
    public void givenABatchWithSomeCompleteItemsAndSomeSubmittedItemsThenTheBatchStatusIsRunning() {
        BatchRepository repository = givenABatchWithStatuses(DownloadStatus.SUCCESS, DownloadStatus.SUBMITTED);

        int batchStatus = repository.getBatchStatus(1);

        assertThat(batchStatus).isEqualTo(DownloadStatus.RUNNING);
    }

    @Test
    public void givenABatchWithItemsThatAreNotSubmittedOrSuccessThenTheBatchStatusIsNotRunning() {
        BatchRepository repository = givenABatchWithStatuses(DownloadStatus.SUCCESS, DownloadStatus.BATCH_FAILED);

        int batchStatus = repository.getBatchStatus(1);

        assertThat(batchStatus).isNotEqualTo(DownloadStatus.RUNNING);
    }

    @Test
    public void givenABatchWithSubmittedAndSuccessStatesThenTheBatchStatusIsRunning() {
        BatchRepository repository = givenABatchWithStatuses(DownloadStatus.SUBMITTED, DownloadStatus.SUBMITTED, DownloadStatus.SUBMITTED, DownloadStatus.SUCCESS);

        int batchStatus = repository.getBatchStatus(1);

        assertThat(batchStatus).isEqualTo(DownloadStatus.RUNNING);
    }

    @Test
    public void givenABatchWithAllCompleteItemsThenTheBatchStatusIsSuccess() {
        BatchRepository repository = givenABatchWithStatuses(DownloadStatus.SUCCESS, DownloadStatus.SUCCESS);

        int batchStatus = repository.getBatchStatus(1);

        assertThat(batchStatus).isEqualTo(DownloadStatus.SUCCESS);
    }

    @Test
    public void givenABatchWithAllPendingItemsThenTheBatchStatusIsPending() {
        BatchRepository repository = givenABatchWithStatuses(DownloadStatus.PENDING, DownloadStatus.PENDING);

        int batchStatus = repository.getBatchStatus(1);

        assertThat(batchStatus).isEqualTo(DownloadStatus.PENDING);
    }

    @NonNull
    private BatchRepository givenABatchWithStatuses(Integer... statuses) {
        ContentResolver mockResolver = mock(ContentResolver.class);
        DownloadsUriProvider mockUriProvider = mock(DownloadsUriProvider.class);
        MockCursorWithStatuses mockCursorWithStatuses = new MockCursorWithStatuses(statuses);
        when(mockResolver.query(eq(mockUriProvider.getAllDownloadsUri()), any(String[].class), anyString(), any(String[].class), anyString()))
                .thenReturn(mockCursorWithStatuses);
        return new BatchRepository(mockResolver, null, mockUriProvider, null);
    }

    private static class MockCursorWithStatuses implements Cursor {

        private final List<Integer> statusList;
        private int position = -1;

        public MockCursorWithStatuses(Integer... statuses) {
            this.statusList = new ArrayList<>();
            Collections.addAll(statusList, statuses);
        }

        @Override
        public int getCount() {
            return statusList.size();
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public boolean move(int i) {
            return position + i >= 0 && position + i < statusList.size();
        }

        @Override
        public boolean moveToPosition(int i) {
            return i >= 0 && i < statusList.size();
        }

        @Override
        public boolean moveToFirst() {
            position = 0;
            return true;
        }

        @Override
        public boolean moveToLast() {
            position = statusList.size() - 1;
            return false;
        }

        @Override
        public boolean moveToNext() {
            if (position < statusList.size() - 1) {
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
            return position == statusList.size() - 1;
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
            return statusList.get(position);
        }

        @Override
        public long getLong(int i) {
            return 0;
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
            return false;
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
