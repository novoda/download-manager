package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class BatchRetrievalRepositoryTest {

    @Mock
    private ContentResolver mockContentResolver;
    @Mock
    private FileDownloadInfo mockFileDownloadInfo;
    @Mock
    private DownloadsUriProvider mockDownloadsUriProvider;
    @Mock
    private Uri mockUri;

    private BatchRetrievalRepository batchRetrievalRepository;

    @Before
    public void setUp() {
        initMocks(this);

        when(mockDownloadsUriProvider.getBatchesUri()).thenReturn(mockUri);
        when(mockDownloadsUriProvider.getAllDownloadsUri()).thenReturn(mockUri);

        batchRetrievalRepository = new BatchRetrievalRepository(mockContentResolver, mockDownloadsUriProvider);
    }

    @Test
    public void givenADownloadInfoWhenRetrievingTheBatchThenTheBatchIdsMatch() {
        long expectedBatchId = 100L;
        when(mockFileDownloadInfo.getBatchId()).thenReturn(expectedBatchId);
        Cursor batchCursor = new MockCursorWithBatchIds(Collections.singletonList(expectedBatchId));
        when(mockContentResolver.query(any(Uri.class), any(String[].class), anyString(), any(String[].class), anyString())).thenReturn(batchCursor);

        DownloadBatch downloadBatch = batchRetrievalRepository.retrieveBatchFor(mockFileDownloadInfo);

        assertThat(downloadBatch.getBatchId()).isEqualTo(expectedBatchId);
    }

    @Test
    public void givenADownloadInfoAndNoLinkedBatchesWhenRetrievingTheBatchThenTheBatchIsDeleted() {
        long batchIdToBeMissing = 100L;
        when(mockFileDownloadInfo.getBatchId()).thenReturn(batchIdToBeMissing);
        Cursor emptyBatchCursor = mock(Cursor.class);
        when(mockContentResolver.query(any(Uri.class), any(String[].class), anyString(), any(String[].class), anyString())).thenReturn(emptyBatchCursor);

        DownloadBatch downloadBatch = batchRetrievalRepository.retrieveBatchFor(mockFileDownloadInfo);

        assertThat(downloadBatch).isEqualTo(DownloadBatch.DELETED);
    }

    @Test
    public void givenABatchQueryWhenQueryingThenTheQueryIsUsed() {
        BatchQuery query = new BatchQuery.Builder().withId(12).build();

        batchRetrievalRepository.retrieveFor(query);

        String selection = query.getSelection();
        String[] selectionArguments = query.getSelectionArguments();
        String sortOrder = query.getSortOrder();
        verify(mockContentResolver).query(any(Uri.class), any(String[].class), eq(selection), eq(selectionArguments), eq(sortOrder));
    }

}
