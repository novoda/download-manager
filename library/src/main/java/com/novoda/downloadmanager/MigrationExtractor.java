package com.novoda.downloadmanager;

import android.database.Cursor;

import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.List;

class MigrationExtractor {

    private static final String BATCHES_QUERY = "SELECT batches._id, batches.batch_title FROM batches INNER JOIN DownloadsByBatch ON " +
            "DownloadsByBatch.batch_id = batches._id WHERE DownloadsByBatch.batch_total_bytes = DownloadsByBatch.batch_current_bytes";
    private static final String URIS_QUERY = "SELECT uri, _data, total_bytes FROM Downloads WHERE batch_id = ?";
    private static final int FIRST_COLUMN = 0;
    private static final int SECOND_COLUMN = 1;
    private static final int THIRD_COLUMN = 2;

    private final SqlDatabaseWrapper database;

    MigrationExtractor(SqlDatabaseWrapper database) {
        this.database = database;
    }

    List<Migration> extractMigrations() {
        Cursor batchesCursor = database.rawQuery(BATCHES_QUERY);

        List<Migration> migrations = new ArrayList<>();
        while (batchesCursor.moveToNext()) {

            String batchId = batchesCursor.getString(FIRST_COLUMN);
            String batchTitle = batchesCursor.getString(SECOND_COLUMN);

            Cursor uriCursor = database.rawQuery(URIS_QUERY, batchId);
            Batch.Builder newBatchBuilder = new Batch.Builder(DownloadBatchIdCreator.createFrom(batchId), batchTitle);

            List<String> originalFileLocations = new ArrayList<>();
            List<FileSize> fileSizes = new ArrayList<>();

            while (uriCursor.moveToNext()) {
                String uri = uriCursor.getString(FIRST_COLUMN);
                String originalFileName = uriCursor.getString(SECOND_COLUMN);
                Log.d("MainActivity", batchId + " : " + batchTitle + " : " + uri);

                newBatchBuilder.addFile(uri);

                originalFileLocations.add(originalFileName);

                long rawFileSize = uriCursor.getLong(THIRD_COLUMN);
                FileSize fileSize = new LiteFileSize(rawFileSize, rawFileSize);
                fileSizes.add(fileSize);
            }

            uriCursor.close();

            Batch batch = newBatchBuilder.build();
            migrations.add(new Migration(batch, originalFileLocations, fileSizes));
        }
        batchesCursor.close();
        return migrations;
    }

}
