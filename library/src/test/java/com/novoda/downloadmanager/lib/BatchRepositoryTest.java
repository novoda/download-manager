package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BatchRepositoryTest {

    BatchRepository batchRepository;

    @Mock
    ContentResolver contentResolver;

    @Mock
    DownloadDeleter downloadDeleter;

    @Mock
    FileDownloadInfo fileDownloadInfo;

    @Before
    public void setUp() {
        initMocks(this);
        this.batchRepository = new BatchRepository(contentResolver, downloadDeleter, mock(Uri.class), mock(Uri.class));
    }

    @Test
    public void givenADownloadInfoWhenRetrievingTheBatchThenTheBatchIdsMatch() {
        long expectedBatchId = 100L;
        when(fileDownloadInfo.getBatchId()).thenReturn(expectedBatchId);
        Cursor batchCursor = singleBatchCursor(expectedBatchId);
        when(contentResolver.query(any(Uri.class), any(String[].class), anyString(), any(String[].class), anyString())).thenReturn(batchCursor);

        DownloadBatch downloadBatch = batchRepository.retrieveBatchFor(fileDownloadInfo);

        assertThat(downloadBatch.getBatchId()).isEqualTo(expectedBatchId);
    }

    private Cursor singleBatchCursor(final long batchId) {
        final int batchSize = 1;
        int idColumn = 1337;
        Cursor cursor = mock(Cursor.class);
        when(cursor.getColumnIndexOrThrow(Downloads.Impl.Batches._ID)).thenReturn(idColumn);
        when(cursor.moveToNext()).thenAnswer(new Answer<Boolean>() {
            int count = 0;

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                boolean batchSizeNotReached = count < batchSize;
                count++;
                return batchSizeNotReached;
            }
        });

        when(cursor.getLong(idColumn)).thenReturn(batchId);

        return cursor;
    }

    @Test
    public void givenADownloadInfoAndNoLinkedBatchesWhenRetrievingTheBatchThenTheBatchIsDeleted() {
        long batchIdToBeMissing = 100L;
        when(fileDownloadInfo.getBatchId()).thenReturn(batchIdToBeMissing);
        Cursor emptyBatchCursor = mock(Cursor.class);
        when(contentResolver.query(any(Uri.class), any(String[].class), anyString(), any(String[].class), anyString())).thenReturn(emptyBatchCursor);

        DownloadBatch downloadBatch = batchRepository.retrieveBatchFor(fileDownloadInfo);

        assertThat(downloadBatch).isEqualTo(DownloadBatch.DELETED);
    }
}
