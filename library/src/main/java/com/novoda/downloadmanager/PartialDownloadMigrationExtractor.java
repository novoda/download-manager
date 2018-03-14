package com.novoda.downloadmanager;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PartialDownloadMigrationExtractor {

    private static final String BATCHES_QUERY = "SELECT batches._id, batches.batch_title, batches.last_modified_timestamp "
            + "FROM batches INNER JOIN DownloadsByBatch ON DownloadsByBatch.batch_id = batches._id "
            + "WHERE DownloadsByBatch.batch_total_bytes != DownloadsByBatch.batch_current_bytes "
            + "OR DownloadsByBatch._data IS NULL "
            + "GROUP BY batches._id";

    private static final int BATCH_ID_COLUMN = 0;
    private static final int TITLE_COLUMN = 1;
    private static final int MODIFIED_TIMESTAMP_COLUMN = 2;

    private static final String DOWNLOADS_QUERY = "SELECT uri, notificationextras FROM Downloads WHERE batch_id = ?";
    private static final int URI_COLUMN = 0;
    private static final int FILE_ID_COLUMN = 1;
    private static final String UNKNOWN_ORIGINAL_LOCATION = "";

    private final SqlDatabaseWrapper database;

    PartialDownloadMigrationExtractor(SqlDatabaseWrapper database) {
        this.database = database;
    }

    List<Migration> extractMigrations() {
        Cursor batchesCursor = database.rawQuery(BATCHES_QUERY);

        List<Migration> migrations = new ArrayList<>();
        while (batchesCursor.moveToNext()) {

            String batchId = batchesCursor.getString(BATCH_ID_COLUMN);
            String batchTitle = batchesCursor.getString(TITLE_COLUMN);
            long downloadedDateTimeInMillis = batchesCursor.getLong(MODIFIED_TIMESTAMP_COLUMN);

            Cursor downloadsCursor = database.rawQuery(DOWNLOADS_QUERY, batchId);
            Batch.Builder newBatchBuilder = null;
            List<Migration.FileMetadata> fileMetadataList = new ArrayList<>();
            Set<String> uris = new HashSet<>();

            while (downloadsCursor.moveToNext()) {
                String originalFileId = downloadsCursor.getString(FILE_ID_COLUMN);
                String uri = downloadsCursor.getString(URI_COLUMN);

                if (downloadsCursor.isFirst()) {
                    DownloadBatchId downloadBatchId = createDownloadBatchIdFrom(originalFileId, batchId);
                    newBatchBuilder = Batch.with(downloadBatchId, batchTitle);
                }

                if (uris.contains(uri)) {
                    continue;
                } else {
                    uris.add(uri);
                }
                newBatchBuilder.addFile(uri).apply();

                FileSize fileSize = FileSizeCreator.unknownFileSize();
                Migration.FileMetadata fileMetadata = new Migration.FileMetadata(originalFileId, UNKNOWN_ORIGINAL_LOCATION, fileSize, uri);
                fileMetadataList.add(fileMetadata);
            }
            downloadsCursor.close();

            Batch batch = newBatchBuilder.build();
            migrations.add(new Migration(batch, fileMetadataList, downloadedDateTimeInMillis, Migration.Type.PARTIAL));
        }
        batchesCursor.close();
        return migrations;
    }

    private DownloadBatchId createDownloadBatchIdFrom(String originalFileId, String batchId) {
        if (originalFileId == null || originalFileId.isEmpty()) {
            String hashedString = String.valueOf(batchId.hashCode());
            return DownloadBatchIdCreator.createFrom(hashedString);
        }
        String hashedString = String.valueOf(originalFileId.hashCode());
        return DownloadBatchIdCreator.createFrom(hashedString);
    }
}
