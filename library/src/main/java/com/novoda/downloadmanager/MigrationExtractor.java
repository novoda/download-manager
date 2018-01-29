package com.novoda.downloadmanager;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MigrationExtractor {

    private static final String BATCHES_QUERY = "SELECT batches._id, batches.batch_title, batches.last_modified_timestamp FROM "
            + "batches INNER JOIN DownloadsByBatch ON DownloadsByBatch.batch_id = batches._id "
            + "WHERE DownloadsByBatch.batch_total_bytes = DownloadsByBatch.batch_current_bytes GROUP BY batches._id";
    private static final int BATCH_ID_COLUMN = 0;
    private static final int TITLE_COLUMN = 1;
    private static final int MODIFIED_TIMESTAMP_COLUMN = 2;

    private static final String DOWNLOADS_QUERY = "SELECT uri, _data, total_bytes FROM Downloads WHERE batch_id = ?";
    private static final int NETWORK_ADDRESS_COLUMN = 0;
    private static final int FILE_LOCATION_COLUMN = 1;
    private static final int FILE_SIZE_COLUMN = 2;

    private final SqlDatabaseWrapper database;

    MigrationExtractor(SqlDatabaseWrapper database) {
        this.database = database;
    }

    List<Migration> extractMigrations() {
        Cursor batchesCursor = database.rawQuery(BATCHES_QUERY);

        if (batchesCursor == null) {
            return Collections.emptyList();
        }

        try {
            List<Migration> migrations = new ArrayList<>();

            while (batchesCursor.moveToNext()) {
                String batchId = batchesCursor.getString(BATCH_ID_COLUMN);
                String batchTitle = batchesCursor.getString(TITLE_COLUMN);
                long downloadedDateTimeInMillis = batchesCursor.getLong(MODIFIED_TIMESTAMP_COLUMN);

                Batch.Builder newBatchBuilder = new Batch.Builder(DownloadBatchIdCreator.createFrom(batchId), batchTitle);
                List<Migration.FileMetadata> fileMetadataList = extractFileMetadataFrom(batchId, newBatchBuilder);

                Batch batch = newBatchBuilder.build();
                migrations.add(new Migration(batch, fileMetadataList, downloadedDateTimeInMillis));
            }

            return migrations;
        } finally {
            batchesCursor.close();
        }
    }

    private List<Migration.FileMetadata> extractFileMetadataFrom(String batchId, Batch.Builder newBatchBuilder) {
        Cursor downloadsCursor = database.rawQuery(DOWNLOADS_QUERY, batchId);

        if (downloadsCursor == null) {
            return Collections.emptyList();
        }

        try {
            List<Migration.FileMetadata> fileMetadataList = new ArrayList<>();

            while (downloadsCursor.moveToNext()) {
                String originalNetworkAddress = downloadsCursor.getString(NETWORK_ADDRESS_COLUMN);
                String originalFileLocation = downloadsCursor.getString(FILE_LOCATION_COLUMN);
                newBatchBuilder.addFile(originalNetworkAddress);

                long rawFileSize = downloadsCursor.getLong(FILE_SIZE_COLUMN);
                FileSize fileSize = new LiteFileSize(rawFileSize, rawFileSize);
                Migration.FileMetadata fileMetadata = new Migration.FileMetadata(originalFileLocation, fileSize, originalNetworkAddress);
                fileMetadataList.add(fileMetadata);
            }
            return fileMetadataList;
        } finally {
            downloadsCursor.close();
        }
    }
}
