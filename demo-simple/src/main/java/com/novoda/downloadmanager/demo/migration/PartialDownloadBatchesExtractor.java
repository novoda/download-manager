package com.novoda.downloadmanager.demo.migration;

import android.database.Cursor;

import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.BatchBuilder;
import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.DownloadBatchIdCreator;
import com.novoda.downloadmanager.DownloadFileIdCreator;
import com.novoda.downloadmanager.SqlDatabaseWrapper;
import com.novoda.downloadmanager.StorageRoot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PartialDownloadBatchesExtractor {

    private static final String BATCHES_QUERY = "SELECT batches._id, batches.batch_title "
            + "FROM batches INNER JOIN DownloadsByBatch ON DownloadsByBatch.batch_id = batches._id "
            + "WHERE DownloadsByBatch.batch_total_bytes != DownloadsByBatch.batch_current_bytes "
            + "OR DownloadsByBatch._data IS NULL "
            + "GROUP BY batches._id";

    private static final int BATCH_ID_COLUMN = 0;
    private static final int TITLE_COLUMN = 1;

    private static final String DOWNLOADS_QUERY = "SELECT uri, notificationextras, hint FROM Downloads WHERE batch_id = ?";
    private static final int URI_COLUMN = 0;
    private static final int FILE_ID_COLUMN = 1;
    private static final int FILE_LOCATION_COLUMN = 2;

    private final SqlDatabaseWrapper database;
    private final StorageRoot storageRoot;

    PartialDownloadBatchesExtractor(SqlDatabaseWrapper database, StorageRoot storageRoot) {
        this.database = database;
        this.storageRoot = storageRoot;
    }

    List<VersionOnePartialDownloadBatch> extractMigrations() {
        Cursor batchesCursor = database.rawQuery(BATCHES_QUERY);

        List<VersionOnePartialDownloadBatch> partialMigrations = new ArrayList<>();
        while (batchesCursor.moveToNext()) {

            String batchId = batchesCursor.getString(BATCH_ID_COLUMN);
            String batchTitle = batchesCursor.getString(TITLE_COLUMN);

            Cursor downloadsCursor = database.rawQuery(DOWNLOADS_QUERY, batchId);
            BatchBuilder newBatchBuilder = null;
            Set<String> uris = new HashSet<>();
            Set<String> fileIds = new HashSet<>();

            DownloadBatchId downloadBatchId;
            List<String> originalFileLocations = new ArrayList<>();
            while (downloadsCursor.moveToNext()) {
                String originalFileId = downloadsCursor.getString(FILE_ID_COLUMN);
                String uri = downloadsCursor.getString(URI_COLUMN);
                String originalFileLocation = downloadsCursor.getString(FILE_LOCATION_COLUMN);
                String sanitizedOriginalFileLocation = MigrationStoragePathSanitizer.sanitize(originalFileLocation);
                originalFileLocations.add(sanitizedOriginalFileLocation);

                if (downloadsCursor.isFirst()) {
                    downloadBatchId = createDownloadBatchIdFrom(originalFileId, batchId);
                    newBatchBuilder = Batch.with(storageRoot, downloadBatchId, batchTitle);
                }

                if (uris.contains(uri) && fileIds.contains(originalFileId)) {
                    continue;
                } else {
                    uris.add(uri);
                    fileIds.add(originalFileId);
                }

                if (originalFileId == null) {
                    newBatchBuilder.downloadFrom(uri)
                            .apply();
                } else {
                    newBatchBuilder.downloadFrom(uri)
                            .withIdentifier(DownloadFileIdCreator.createFrom(originalFileId))
                            .apply();
                }
            }
            downloadsCursor.close();

            Batch batch = newBatchBuilder.build();
            partialMigrations.add(new VersionOnePartialDownloadBatch(batch, originalFileLocations));
        }
        batchesCursor.close();
        return partialMigrations;
    }

    private DownloadBatchId createDownloadBatchIdFrom(String originalFileId, String batchId) {
        if (originalFileId == null || originalFileId.isEmpty()) {
            String hashedString = String.valueOf(batchId.hashCode());
            return DownloadBatchIdCreator.createSanitizedFrom(hashedString);
        }
        String hashedString = String.valueOf(originalFileId.hashCode());
        return DownloadBatchIdCreator.createSanitizedFrom(hashedString);
    }
}
