package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.novoda.downloadmanager.lib.DownloadContract.Batches;
import com.novoda.downloadmanager.lib.DownloadContract.Downloads;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InOrder;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collection;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(Enclosed.class)
public class BatchStatusTests {

    private static final long ANY_BATCH_ID = 1;
    private static final Uri ACCESSIBLE_DOWNLOADS_URI = mock(Uri.class);
    private static final Uri DOWNLOADS_BY_BATCH_URI = mock(Uri.class);
    private static final Uri ALL_DOWNLOADS_URI = mock(Uri.class);
    private static final Uri BATCHES_URI = mock(Uri.class);
    private static final Uri BATCH_BY_ID_URI = mock(Uri.class);

    private static final Uri CONTENT_URI = mock(Uri.class);
    private static final Uri DOWNLOADS_WITHOUT_PROGRESS_URI = mock(Uri.class);

    private static final Uri BATCHES_WITHOUT_PROGRESS_URI = mock(Uri.class);
    public static final long CURRENT_TIME_MILLIS = 1l;

    @RunWith(PowerMockRunner.class)
    @PrepareForTest({BatchRepository.class})
    public static class UpdateBatchStatus {

        @Test
        public void whenUpdatingABatchStatusThenTheCorrectBatchIsUpdated() throws Exception {
            final ContentValues mockContentValues = mock(ContentValues.class);
            whenNew(ContentValues.class).withAnyArguments().thenReturn(mockContentValues);

            ContentResolver mockResolver = mock(ContentResolver.class);
            BatchRepository batchRepository = givenBatchRepositoryAtCurrentTime(mockResolver);

            batchRepository.updateBatchStatus(ANY_BATCH_ID, DownloadStatus.BATCH_FAILED);

            verify(mockContentValues).put(Batches.COLUMN_STATUS, DownloadStatus.BATCH_FAILED);
            verify(mockContentValues).put(Batches.COLUMN_LAST_MODIFICATION, CURRENT_TIME_MILLIS);
            verify(mockResolver).update(
                    BATCHES_URI,
                    mockContentValues,
                    Batches._ID + " = ?",
                    new String[]{String.valueOf(ANY_BATCH_ID)}
            );
        }
    }

    @RunWith(Parameterized.class)
    public static class GetBatchStatus {

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {DownloadStatus.PAUSING},
                    {DownloadStatus.QUEUED_DUE_CLIENT_RESTRICTIONS},
                    {DownloadStatus.DELETING},
                    {DownloadStatus.SUBMITTED},
                    {DownloadStatus.PENDING},
                    {DownloadStatus.RUNNING},
                    {DownloadStatus.PAUSED_BY_APP},
                    {DownloadStatus.WAITING_TO_RETRY},
                    {DownloadStatus.WAITING_FOR_NETWORK},
                    {DownloadStatus.QUEUED_FOR_WIFI},
                    {DownloadStatus.INSUFFICIENT_SPACE_ERROR},
                    {DownloadStatus.DEVICE_NOT_FOUND_ERROR},
                    {DownloadStatus.SUCCESS},
                    {DownloadStatus.BAD_REQUEST},
                    {DownloadStatus.NOT_ACCEPTABLE},
                    {DownloadStatus.LENGTH_REQUIRED},
                    {DownloadStatus.PRECONDITION_FAILED},
                    {DownloadStatus.MIN_ARTIFICIAL_ERROR_STATUS},
                    {DownloadStatus.FILE_ALREADY_EXISTS_ERROR},
                    {DownloadStatus.CANNOT_RESUME},
                    {DownloadStatus.CANCELED},
                    {DownloadStatus.UNKNOWN_ERROR},
                    {DownloadStatus.FILE_ERROR},
                    {DownloadStatus.UNHANDLED_REDIRECT},
                    {DownloadStatus.UNHANDLED_HTTP_CODE},
                    {DownloadStatus.HTTP_DATA_ERROR},
                    {DownloadStatus.HTTP_EXCEPTION},
                    {DownloadStatus.TOO_MANY_REDIRECTS},
                    {DownloadStatus.BATCH_FAILED},
            });
        }

        private int expectedStatus;

        public GetBatchStatus(int expectedStatus) {
            this.expectedStatus = expectedStatus;
        }

        @Test
        public void givenAStatusThenThatStatusIsRetrieved() throws Exception {
            BatchRepository repository = givenABatchWithStatuses(BATCHES_URI, expectedStatus);

            int batchStatus = repository.getBatchStatus(ANY_BATCH_ID);

            assertThat(batchStatus).isEqualTo(expectedStatus);
        }

    }

    @RunWith(Parameterized.class)
    public static class CalculateBatchStatus {

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {new Integer[]{DownloadStatus.SUCCESS, DownloadStatus.SUBMITTED}, DownloadStatus.RUNNING},
                    {new Integer[]{DownloadStatus.SUCCESS, DownloadStatus.BATCH_FAILED}, DownloadStatus.BATCH_FAILED},
                    {new Integer[]{DownloadStatus.SUBMITTED, DownloadStatus.SUBMITTED, DownloadStatus.SUBMITTED, DownloadStatus.SUCCESS}, DownloadStatus.RUNNING},
                    {new Integer[]{DownloadStatus.SUCCESS, DownloadStatus.SUCCESS}, DownloadStatus.SUCCESS},
                    {new Integer[]{DownloadStatus.PENDING, DownloadStatus.PENDING}, DownloadStatus.PENDING},
            });
        }

        private Integer[] statuses;

        private int expectedStatus;

        public CalculateBatchStatus(Integer[] statuses, int expectedStatus) {
            this.statuses = statuses;
            this.expectedStatus = expectedStatus;
        }

        @Test
        public void calculateCorrectStatusFromStatuses() throws Exception {
            BatchRepository repository = givenABatchWithStatuses(ALL_DOWNLOADS_URI, statuses);

            int batchStatus = repository.calculateBatchStatus(ANY_BATCH_ID);

            assertThat(batchStatus).isEqualTo(expectedStatus);
        }
    }

    @RunWith(PowerMockRunner.class)
    @PrepareForTest({ContentUris.class, BatchRepository.class})
    public static class CancelBatch {

        @Test
        public void whenCancellingBatchItemsThenCorrectDownloadsAreCancelled() throws Exception {
            final ContentValues mockContentValues = mock(ContentValues.class);
            whenNew(ContentValues.class).withAnyArguments().thenReturn(mockContentValues);

            ContentResolver mockResolver = mock(ContentResolver.class);
            BatchRepository batchRepository = givenBatchRepositoryAtCurrentTime(mockResolver);

            batchRepository.setBatchItemsCancelled(ANY_BATCH_ID);

            verify(mockContentValues).put(Downloads.COLUMN_STATUS, DownloadStatus.CANCELED);
            verify(mockResolver).update(
                    ALL_DOWNLOADS_URI,
                    mockContentValues,
                    Downloads.COLUMN_BATCH_ID + " = ?",
                    new String[]{String.valueOf(ANY_BATCH_ID)}
            );
        }

        @Test
        public void whenCancellingBatchThenCorrectBatchAndDownloadsAreCancelled() throws Exception {
            mockStatic(ContentUris.class);
            when(ContentUris.withAppendedId(BATCHES_URI, ANY_BATCH_ID)).thenReturn(BATCH_BY_ID_URI);

            final ContentValues mockContentValues = mock(ContentValues.class);
            whenNew(ContentValues.class).withAnyArguments().thenReturn(mockContentValues);
            ContentResolver mockResolver = mock(ContentResolver.class);
            BatchRepository batchRepository = givenBatchRepositoryAtCurrentTime(mockResolver);

            batchRepository.cancelBatch(ANY_BATCH_ID);

            InOrder inorder = inOrder(mockContentValues, mockResolver);

            inorder.verify(mockContentValues).put(Downloads.COLUMN_STATUS, DownloadStatus.CANCELED);
            inorder.verify(mockResolver).update(
                    ALL_DOWNLOADS_URI,
                    mockContentValues,
                    Downloads.COLUMN_BATCH_ID + " = ?",
                    new String[]{String.valueOf(ANY_BATCH_ID)}
            );

            inorder.verify(mockContentValues).put(Batches.COLUMN_STATUS, DownloadStatus.CANCELED);
            inorder.verify(mockResolver).update(BATCH_BY_ID_URI, mockContentValues, null, null);
        }
    }

    @NonNull
    private static BatchRepository givenABatchWithStatuses(Uri atUri, Integer... statuses) throws Exception {
        MockCursorWithStatuses mockCursorWithStatuses = new MockCursorWithStatuses(statuses);
        ContentResolver mockResolver = mock(ContentResolver.class);
        when(mockResolver.query(same(atUri), any(String[].class), anyString(), any(String[].class), anyString()))
                .thenReturn(mockCursorWithStatuses);
        return givenBatchRepositoryAtCurrentTime(mockResolver);
    }

    @NonNull
    private static BatchRepository givenBatchRepositoryAtCurrentTime(final ContentResolver mockResolver) throws Exception {
        DownloadsUriProvider downloadsUriProvider = givenDownloadsUriProvider();
        SystemFacade mockSystemFacade = mock(SystemFacade.class);
        when(mockSystemFacade.currentTimeMillis()).thenReturn(CURRENT_TIME_MILLIS);

        return new BatchRepository(mockResolver, null, downloadsUriProvider, mockSystemFacade);
    }

    private static DownloadsUriProvider givenDownloadsUriProvider() {
        return new DownloadsUriProvider(
                ACCESSIBLE_DOWNLOADS_URI,
                DOWNLOADS_BY_BATCH_URI,
                ALL_DOWNLOADS_URI,
                BATCHES_URI,
                CONTENT_URI,
                DOWNLOADS_WITHOUT_PROGRESS_URI,
                BATCHES_WITHOUT_PROGRESS_URI
        );
    }
}
