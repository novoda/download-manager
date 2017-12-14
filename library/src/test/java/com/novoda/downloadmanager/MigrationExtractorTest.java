package com.novoda.downloadmanager;

import android.database.Cursor;

import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MigrationExtractorTest {

    private static final String BATCHES_QUERY = "SELECT DISTINCT batches._id, batches.batch_title FROM batches INNER JOIN DownloadsByBatch ON " +
            "DownloadsByBatch.batch_id = batches._id WHERE DownloadsByBatch.batch_total_bytes = DownloadsByBatch.batch_current_bytes";

    private static final String DOWNLOADS_QUERY = "SELECT uri, _data, total_bytes FROM Downloads WHERE batch_id = ?";

    private static final StubCursor BATCHES_CURSOR = new StubCursor.Builder()
            .with("_id", "1", "2")
            .with("batch_title", "title_1", "title_2")
            .build();

    private static final Cursor BATCH_ONE_DOWNLOADS_CURSOR = new StubCursor.Builder()
            .with("uri", "uri_1", "uri_2")
            .with("_data", "data_1", "data_2")
            .with("total_bytes", "1000", "2000")
            .build();

    private static final Cursor BATCH_TWO_DOWNLOADS_CURSOR = new StubCursor.Builder()
            .with("uri", "uri_3", "uri_4")
            .with("_data", "data_3", "data_4")
            .with("total_bytes", "500", "750")
            .build();

    private final SqlDatabaseWrapper database = mock(SqlDatabaseWrapper.class);

    private MigrationExtractor migrationExtractor;

    @Before
    public void setUp() {
        Log.setShowLogs(false);
        migrationExtractor = new MigrationExtractor(database);
    }

    @Test
    public void returnsMigrations_WhenExtracting() {
        given(database.rawQuery(BATCHES_QUERY)).willReturn(BATCHES_CURSOR);
        given(database.rawQuery(eq(DOWNLOADS_QUERY), eq("1"))).willReturn(BATCH_ONE_DOWNLOADS_CURSOR);
        given(database.rawQuery(eq(DOWNLOADS_QUERY), eq("2"))).willReturn(BATCH_TWO_DOWNLOADS_CURSOR);

        List<Migration> migrations = migrationExtractor.extractMigrations();

        assertThat(migrations).isEqualTo(expectedMigrations());
    }

    private List<Migration> expectedMigrations() {
        String firstUri = "uri_1";
        String secondUri = "uri_2";
        Batch firstBatch = new Batch.Builder(DownloadBatchIdCreator.createFrom("1"), "title_1")
                .addFile(firstUri)
                .addFile(secondUri)
                .build();

        List<Migration.FileMetadata> firstFileMetadata = new ArrayList<>();
        firstFileMetadata.add(new Migration.FileMetadata("data_1", new LiteFileSize(1000, 1000), firstUri));
        firstFileMetadata.add(new Migration.FileMetadata("data_2", new LiteFileSize(2000, 2000), secondUri));

        String thirdUri = "uri_3";
        String fourthUri = "uri_4";
        Batch secondBatch = new Batch.Builder(DownloadBatchIdCreator.createFrom("2"), "title_2")
                .addFile(thirdUri)
                .addFile(fourthUri)
                .build();

        List<Migration.FileMetadata> secondFileMetadata = new ArrayList<>();
        secondFileMetadata.add(new Migration.FileMetadata("data_3", new LiteFileSize(500, 500), thirdUri));
        secondFileMetadata.add(new Migration.FileMetadata("data_4", new LiteFileSize(750, 750), fourthUri));

        return Arrays.asList(
                new Migration(firstBatch, firstFileMetadata),
                new Migration(secondBatch, secondFileMetadata)
        );
    }
}
