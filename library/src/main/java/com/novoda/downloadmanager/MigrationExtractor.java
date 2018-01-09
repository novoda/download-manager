package com.novoda.downloadmanager;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

class MigrationExtractor {

    private static final String BATCHES_QUERY = "SELECT batches._id, batches.batch_title FROM batches INNER JOIN DownloadsByBatch ON "
            + "DownloadsByBatch.batch_id = batches._id WHERE DownloadsByBatch.batch_total_bytes = DownloadsByBatch.batch_current_bytes "
            + "GROUP BY batches._id";
    private static final String DOWNLOADS_QUERY = "SELECT uri, _data, total_bytes, last_modified_timestamp FROM Downloads WHERE batch_id = ?";
    private static final int BATCH_ID_COLUMN = 0;
    private static final int URI_COLUMN = 0;
    private static final int TITLE_COLUMN = 1;
    private static final int FILE_NAME_COLUMN = 1;
    private static final int FILE_SIZE_COLUMN = 2;
    private static final int LAST_MODIFIED_COLUMN = 3;

    private final SqlDatabaseWrapper database;

    MigrationExtractor(SqlDatabaseWrapper database) {
        this.database = database;
    }

    List<Migration> extractMigrations() {
        Cursor batchesCursor = database.rawQuery(BATCHES_QUERY);

        List<Migration> migrations = new ArrayList<>();
        while (batchesCursor.moveToNext()) {

            String batchId = batchesCursor.getString(BATCH_ID_COLUMN);
            String batchTitle = batchesCursor.getString(TITLE_COLUMN);

            Cursor downloadsCursor = database.rawQuery(DOWNLOADS_QUERY, batchId);
            Batch.Builder newBatchBuilder = new Batch.Builder(DownloadBatchIdCreator.createFrom(batchId), batchTitle);
            List<Migration.FileMetadata> fileMetadataList = new ArrayList<>();

            while (downloadsCursor.moveToNext()) {
                String uri = downloadsCursor.getString(URI_COLUMN);
                String originalFileName = downloadsCursor.getString(FILE_NAME_COLUMN);
                long downloadedDateTimeInMillis = downloadsCursor.getLong(LAST_MODIFIED_COLUMN);
                newBatchBuilder.addFile(uri);

                long rawFileSize = downloadsCursor.getLong(FILE_SIZE_COLUMN);
                FileSize fileSize = new LiteFileSize(rawFileSize, rawFileSize);
                Migration.FileMetadata fileMetadata = new Migration.FileMetadata(originalFileName, fileSize, uri, downloadedDateTimeInMillis);
                fileMetadataList.add(fileMetadata);
            }
            downloadsCursor.close();

            Batch batch = newBatchBuilder.build();
            migrations.add(new Migration(batch, fileMetadataList));
        }
        batchesCursor.close();
        return migrations;
    }
}
