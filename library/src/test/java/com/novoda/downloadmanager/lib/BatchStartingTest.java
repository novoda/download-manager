package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.novoda.downloadmanager.lib.DownloadContract.Batches;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ContentUris.class, BatchStartingService.class})
public class BatchStartingTest {

    private static final Uri ACCESSIBLE_DOWNLOADS_URI = mock(Uri.class);
    private static final Uri DOWNLOADS_BY_BATCH_URI = mock(Uri.class);
    private static final Uri ALL_DOWNLOADS_URI = mock(Uri.class);
    private static final Uri BATCHES_URI = mock(Uri.class);
    private static final Uri BATCH_BY_ID_URI = mock(Uri.class);
    private static final Uri CONTENT_URI = mock(Uri.class);
    private static final Uri DOWNLOADS_WITHOUT_PROGRESS_URI = mock(Uri.class);
    private static final Uri BATCHES_WITHOUT_PROGRESS_URI = mock(Uri.class);

    private static final long ANY_BATCH_ID = 1l;
    public static final int SQL_TRUE = 1;

    private BatchRepository repository;
    private Cursor mockCursor;
    private ContentResolver mockContentResolver;

    @Before
    public void setUp() throws Exception {
        mockStatic(ContentUris.class);
        when(ContentUris.withAppendedId(BATCHES_URI, ANY_BATCH_ID)).thenReturn(BATCH_BY_ID_URI);
        mockContentResolver = mock(ContentResolver.class);

        repository = new BatchRepository(
                mockContentResolver,
                null,
                givenDownloadsUriProvider(),
                null
        );

        mockCursor = mock(Cursor.class);
        when(mockCursor.moveToFirst()).thenReturn(true);

        when(mockContentResolver.query(
                same(BATCH_BY_ID_URI),
                any(String[].class),
                isNull(String.class),
                isNull(String[].class),
                isNull(String.class)
        )).thenReturn(mockCursor);
    }


    @Test
    public void whenQueryingForStartedBatchThenResultIsFalse() throws Exception {
        when(mockCursor.getInt(0)).thenReturn(1);

        boolean hasStarted = repository.isBatchStartingForTheFirstTime(ANY_BATCH_ID);

        assertThat(hasStarted).isFalse();
    }

    @Test
    public void whenQueryingForNotStartedBatchThenResultIsTrue() throws Exception {
        when(mockCursor.getInt(0)).thenReturn(0);

        boolean hasStarted = repository.isBatchStartingForTheFirstTime(ANY_BATCH_ID);

        assertThat(hasStarted).isTrue();
    }

    @Test
    public void whenMarkingBatchAsStartedThenCorrectBatchIsMarked() throws Exception {
        ContentValues mockContentValues = mock(ContentValues.class);
        whenNew(ContentValues.class).withAnyArguments()
                .thenReturn(mockContentValues);

        repository.markBatchAsStarted(ANY_BATCH_ID);

        InOrder order = inOrder(mockContentValues, mockContentResolver);

        order.verify(mockContentValues).put(Batches.COLUMN_HAS_STARTED, SQL_TRUE);
        order.verify(mockContentResolver).update(
                BATCH_BY_ID_URI,
                mockContentValues,
                null,
                null
        );
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
