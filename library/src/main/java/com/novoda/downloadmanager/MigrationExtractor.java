package com.novoda.downloadmanager;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MigrationExtractor {

    private static final String BATCHES_QUERY = "SELECT batches._id, batches.batch_title, batches.last_modified_timestamp FROM "
            + "batches INNER JOIN DownloadsByBatch ON DownloadsByBatch.batch_id = batches._id "
            + "WHERE batches._id NOT IN (SELECT DownloadsByBatch.batch_id FROM DownloadsByBatch "
            + "INNER JOIN batches ON batches._id = DownloadsByBatch.batch_id "
            + "WHERE DownloadsByBatch._data IS NULL "
            + "GROUP BY DownloadsByBatch.batch_id) "
            + "GROUP BY batches._id";

    private static final int BATCH_ID_COLUMN = 0;
    private static final int TITLE_COLUMN = 1;
    private static final int MODIFIED_TIMESTAMP_COLUMN = 2;

    private static final String DOWNLOADS_QUERY = "SELECT uri, hint, notificationextras FROM Downloads WHERE batch_id = ?";
    private static final int NETWORK_ADDRESS_COLUMN = 0;
    private static final int FILE_LOCATION_COLUMN = 1;
    private static final int FILE_ID_COLUMN = 2;

    private final SqlDatabaseWrapper database;
    private final FilePersistence filePersistence;
    private final String basePath;

    MigrationExtractor(SqlDatabaseWrapper database, FilePersistence filePersistence, String basePath) {
        this.database = database;
        this.filePersistence = filePersistence;
        this.basePath = basePath;
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
                Cursor downloadsCursor = database.rawQuery(DOWNLOADS_QUERY, batchId);

                if (downloadsCursor == null) {
                    return Collections.emptyList();
                }

                String batchTitle = batchesCursor.getString(TITLE_COLUMN);
                long downloadedDateTimeInMillis = batchesCursor.getLong(MODIFIED_TIMESTAMP_COLUMN);

                Batch.Builder newBatchBuilder = null;
                List<Migration.FileMetadata> fileMetadataList = new ArrayList<>();
                Set<String> uris = new HashSet<>();

                DownloadBatchId downloadBatchId = null;
                try {
                    while (downloadsCursor.moveToNext()) {
                        String originalFileId = downloadsCursor.getString(FILE_ID_COLUMN);
                        String originalNetworkAddress = downloadsCursor.getString(NETWORK_ADDRESS_COLUMN);
                        String originalFileLocation = downloadsCursor.getString(FILE_LOCATION_COLUMN);
                        String sanitizedOriginalFileLocation = MigrationStoragePathSanitizer.sanitize(originalFileLocation);

                        if (downloadsCursor.isFirst()) {
                            downloadBatchId = createDownloadBatchIdFrom(originalFileId, batchId);
                            newBatchBuilder = Batch.with(downloadBatchId, batchTitle);
                        }

                        if (uris.contains(originalNetworkAddress)) {
                            continue;
                        } else {
                            uris.add(originalNetworkAddress);
                        }
                        newBatchBuilder.addFile(originalNetworkAddress).apply();

                        FilePath originalFilePath = new LiteFilePath(sanitizedOriginalFileLocation);
                        FilePath newFilePath = MigrationPathExtractor.extractMigrationPath(basePath, originalFilePath.path(), downloadBatchId);

                        long rawFileSize = filePersistence.getCurrentSize(originalFilePath);
                        FileSize fileSize = new LiteFileSize(rawFileSize, rawFileSize);
                        Migration.FileMetadata fileMetadata = new Migration.FileMetadata(
                                originalFileId,
                                originalFilePath,
                                newFilePath,
                                fileSize,
                                originalNetworkAddress
                        );
                        fileMetadataList.add(fileMetadata);
                    }
                } finally {
                    downloadsCursor.close();
                }

                Batch batch = newBatchBuilder.build();
                migrations.add(new Migration(batch, fileMetadataList, downloadedDateTimeInMillis, Migration.Type.COMPLETE));
            }

            return migrations;
        } finally {
            batchesCursor.close();
        }
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
