package com.novoda.downloadmanager.demo.migration;

import android.database.Cursor;

import com.google.common.truth.Truth;
import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.CompletedDownloadBatch;
import com.novoda.downloadmanager.DownloadBatchIdCreator;
import com.novoda.downloadmanager.DownloadBatchTitleCreator;
import com.novoda.downloadmanager.DownloadFileIdCreator;
import com.novoda.downloadmanager.FileSizeCreator;
import com.novoda.downloadmanager.FileSizeExtractor;
import com.novoda.downloadmanager.SqlDatabaseWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

public class CompletedDownloadBatchesExtractorTest {

    private static final String BATCHES_QUERY = "SELECT batches._id, batches.batch_title, batches.last_modified_timestamp FROM "
            + "batches INNER JOIN DownloadsByBatch ON DownloadsByBatch.batch_id = batches._id "
            + "WHERE batches._id NOT IN (SELECT DownloadsByBatch.batch_id FROM DownloadsByBatch "
            + "INNER JOIN batches ON batches._id = DownloadsByBatch.batch_id "
            + "WHERE DownloadsByBatch._data IS NULL "
            + "GROUP BY DownloadsByBatch.batch_id) "
            + "GROUP BY batches._id";

    private static final String DOWNLOADS_QUERY = "SELECT uri, _data, hint, notificationextras FROM Downloads WHERE batch_id = ?";

    private static final StubCursor BATCHES_CURSOR = new StubCursor.Builder()
            .with("_id", "1", "2")
            .with("batch_title", "title_1", "title_2")
            .with("last_modified_timestamp", "12345", "67890")
            .build();

    private static final Cursor BATCH_ONE_DOWNLOADS_CURSOR = new StubCursor.Builder()
            .with("uri", "uri_1", "uri_2")
            .with("_data", "base/data_1", "base/data_2-1")
            .with("hint", "base/data_1", "base/data_2")
            .with("notificationextras", "file_1", "file_2")
            .build();

    private static final Cursor BATCH_TWO_DOWNLOADS_CURSOR = new StubCursor.Builder()
            .with("uri", "uri_3", "uri_4")
            .with("_data", "base/data_3-1", "base/data_4")
            .with("hint", "base/data_3", "base/data_4")
            .with("notificationextras", "file_3", "file_4")
            .build();

    private final SqlDatabaseWrapper database = Mockito.mock(SqlDatabaseWrapper.class);
    private final FileSizeExtractor fileSizeExtractor = Mockito.mock(FileSizeExtractor.class);

    private CompletedDownloadBatchesExtractor migrationExtractor;

    @Before
    public void setUp() {
        migrationExtractor = new CompletedDownloadBatchesExtractor(database, "base", fileSizeExtractor);
    }

    @Test
    public void returnsMigrations_WhenExtracting() {
        BDDMockito.given(database.rawQuery(BATCHES_QUERY)).willReturn(BATCHES_CURSOR);
        BDDMockito.given(database.rawQuery(ArgumentMatchers.eq(DOWNLOADS_QUERY), ArgumentMatchers.eq("1"))).willReturn(BATCH_ONE_DOWNLOADS_CURSOR);
        BDDMockito.given(database.rawQuery(ArgumentMatchers.eq(DOWNLOADS_QUERY), ArgumentMatchers.eq("2"))).willReturn(BATCH_TWO_DOWNLOADS_CURSOR);
        BDDMockito.given(fileSizeExtractor.fileSizeFor(ArgumentMatchers.anyString()))
                .willReturn(1000L)
                .willReturn(2000L)
                .willReturn(500L)
                .willReturn(750L);

        List<CompletedDownloadBatch> migrations = migrationExtractor.extractMigrations();

        Truth.assertThat(migrations).isEqualTo(expectedMigrations());
    }

    private List<CompletedDownloadBatch> expectedMigrations() {
        String firstUri = "uri_1";
        String secondUri = "uri_2";
        Batch firstBatch = Batch.with(DownloadBatchIdCreator.createSanitizedFrom(String.valueOf("file_1".hashCode())), "title_1")
                .downloadFrom(firstUri).withIdentifier(DownloadFileIdCreator.createFrom("file_1")).apply()
                .downloadFrom(secondUri).withIdentifier(DownloadFileIdCreator.createFrom("file_2")).apply()
                .build();

        List<CompletedDownloadBatch.CompletedDownloadFile> firstFileMetadata = new ArrayList<>();
        firstFileMetadata.add(new CompletedDownloadBatch.CompletedDownloadFile("file_1", "base/data_1", "base/-1274506706/data_1", FileSizeCreator.createForCompletedDownloadBatch(1000), firstUri));
        firstFileMetadata.add(new CompletedDownloadBatch.CompletedDownloadFile("file_2", "base/data_2-1", "base/-1274506706/data_2", FileSizeCreator.createForCompletedDownloadBatch(2000), secondUri));

        String thirdUri = "uri_3";
        String fourthUri = "uri_4";
        Batch secondBatch = Batch.with(DownloadBatchIdCreator.createSanitizedFrom(String.valueOf("file_3".hashCode())), "title_2")
                .downloadFrom(thirdUri).withIdentifier(DownloadFileIdCreator.createFrom("file_3")).apply()
                .downloadFrom(fourthUri).withIdentifier(DownloadFileIdCreator.createFrom("file_4")).apply()
                .build();

        List<CompletedDownloadBatch.CompletedDownloadFile> secondFileMetadata = new ArrayList<>();
        secondFileMetadata.add(new CompletedDownloadBatch.CompletedDownloadFile("file_3", "base/data_3-1", "base/-1274506704/data_3", FileSizeCreator.createForCompletedDownloadBatch(500), thirdUri));
        secondFileMetadata.add(new CompletedDownloadBatch.CompletedDownloadFile("file_4", "base/data_4", "base/-1274506704/data_4", FileSizeCreator.createForCompletedDownloadBatch(750), fourthUri));

        return Arrays.asList(
                new CompletedDownloadBatch(firstBatch.downloadBatchId(), DownloadBatchTitleCreator.createFrom(firstBatch), 12345, firstFileMetadata),
                new CompletedDownloadBatch(secondBatch.downloadBatchId(), DownloadBatchTitleCreator.createFrom(secondBatch), 67890, secondFileMetadata)
        );
    }
}
