package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.support.annotation.NonNull;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class BatchStatusTests {

    @RunWith(Parameterized.class)
    public static class CalculateBatchStatus {

        private static final long ANY_BATCH_ID = 1;

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {new Integer[]{DownloadStatus.SUCCESS, DownloadStatus.SUBMITTED},  DownloadStatus.RUNNING},
                    {new Integer[]{DownloadStatus.SUCCESS, DownloadStatus.BATCH_FAILED},  DownloadStatus.BATCH_FAILED},
                    {new Integer[]{DownloadStatus.SUBMITTED, DownloadStatus.SUBMITTED, DownloadStatus.SUBMITTED, DownloadStatus.SUCCESS},  DownloadStatus.RUNNING},
                    {new Integer[]{DownloadStatus.SUCCESS, DownloadStatus.SUCCESS},  DownloadStatus.SUCCESS},
                    {new Integer[]{DownloadStatus.PENDING, DownloadStatus.PENDING},  DownloadStatus.PENDING},
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
            BatchRepository repository = givenABatchWithStatuses(statuses);

            int batchStatus = repository.calculateBatchStatus(ANY_BATCH_ID);

            assertThat(batchStatus).isEqualTo(expectedStatus);
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

    }
}
