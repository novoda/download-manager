package com.novoda.downloadmanager.demo;

import android.database.Cursor;

import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.BatchBuilder;
import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.DownloadBatchIdCreator;
import com.novoda.downloadmanager.DownloadFileIdCreator;
import com.novoda.downloadmanager.Migration;
import com.novoda.downloadmanager.SqlDatabaseWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompleteDownloadMigrationExtractor {

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

    private static final String DOWNLOADS_QUERY = "SELECT uri, _data, hint, notificationextras FROM Downloads WHERE batch_id = ?";
    private static final int NETWORK_ADDRESS_COLUMN = 0;
    private static final int FILE_ORIGINAL_LOCATION_COLUMN = 1;
    private static final int FILE_UNIQUE_LOCATION_COLUMN = 2;
    private static final int FILE_ID_COLUMN = 3;

    private final SqlDatabaseWrapper database;
    private final FileSizeExtractor fileSizeExtractor;
    private final String basePath;

    public CompleteDownloadMigrationExtractor(SqlDatabaseWrapper database, FileSizeExtractor fileSizeExtractor, String basePath) {
        this.database = database;
        this.fileSizeExtractor = fileSizeExtractor;
        this.basePath = basePath;
    }

    public List<Migration> extractMigrations() {
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

                BatchBuilder newBatchBuilder = null;
                List<Migration.FileMetadata> fileMetadataList = new ArrayList<>();
                Set<String> uris = new HashSet<>();
                Set<String> fileIds = new HashSet<>();

                DownloadBatchId downloadBatchId = null;
                try {
                    while (downloadsCursor.moveToNext()) {
                        String originalFileId = downloadsCursor.getString(FILE_ID_COLUMN);
                        String originalNetworkAddress = downloadsCursor.getString(NETWORK_ADDRESS_COLUMN);

                        // hint: file:/data/user/0/com.channel4.ondemand/files/all4/23241337
                        String originalUniqueFileLocation = downloadsCursor.getString(FILE_UNIQUE_LOCATION_COLUMN);
                        String sanitizedOriginalUniqueFileLocation = MigrationStoragePathSanitizer.sanitize(originalUniqueFileLocation);

                        // _data: /data/user/0/com.channel4.ondemand/files/all4/23241337-1
                        String originalFileLocation = downloadsCursor.getString(FILE_ORIGINAL_LOCATION_COLUMN);

                        if (downloadsCursor.isFirst()) {
                            downloadBatchId = createDownloadBatchIdFrom(originalFileId, batchId);
                            newBatchBuilder = Batch.with(downloadBatchId, batchTitle);
                        }

                        if (uris.contains(originalNetworkAddress) && fileIds.contains(originalFileId)) {
                            continue;
                        } else {
                            uris.add(originalNetworkAddress);
                            fileIds.add(originalFileId);
                        }

                        if (originalFileId == null) {
                            newBatchBuilder.downloadFrom(originalNetworkAddress)
                                    .apply();
                        } else {
                            newBatchBuilder.downloadFrom(originalNetworkAddress)
                                    .withIdentifier(DownloadFileIdCreator.createFrom(originalFileId))
                                    .apply();
                        }

                        String newFilePath = MigrationPathExtractor.extractMigrationPath(basePath, sanitizedOriginalUniqueFileLocation, downloadBatchId);

                        long rawFileSize = fileSizeExtractor.extract(originalFileLocation);

                        Migration.FileMetadata fileMetadata = new Migration.FileMetadata(
                                originalFileId,
                                originalFileLocation,
                                newFilePath,
                                rawFileSize,
                                rawFileSize,
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
            return DownloadBatchIdCreator.createSanitizedFrom(hashedString);
        }
        String hashedString = String.valueOf(originalFileId.hashCode());
        return DownloadBatchIdCreator.createSanitizedFrom(hashedString);
    }

}
