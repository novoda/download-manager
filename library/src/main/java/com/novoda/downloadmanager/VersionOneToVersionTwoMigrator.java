package com.novoda.downloadmanager;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status;

class VersionOneToVersionTwoMigrator implements Migrator {

    private static final int BUFFER_SIZE = 8 * 512;
    private static final String DELETE_QUERY = "DELETE FROM batches WHERE _id = ?";

    private final MigrationExtractor migrationExtractor;
    private final RoomDownloadsPersistence downloadsPersistence;
    private final InternalFilePersistence internalFilePersistence;
    private final SqlDatabaseWrapper database;
    private final Callback migrationCompleteCallback;

    VersionOneToVersionTwoMigrator(MigrationExtractor migrationExtractor,
                                   RoomDownloadsPersistence downloadsPersistence,
                                   InternalFilePersistence internalFilePersistence,
                                   SqlDatabaseWrapper database,
                                   Callback migrationCompleteCallback) {
        this.migrationExtractor = migrationExtractor;
        this.downloadsPersistence = downloadsPersistence;
        this.internalFilePersistence = internalFilePersistence;
        this.database = database;
        this.migrationCompleteCallback = migrationCompleteCallback;
    }

    @Override
    public void migrate() {
        List<Migration> migrations = migrationExtractor.extractMigrations();

        migrateV1FilesToV2Location(migrations);
        migrateV1DataToV2Database(migrations);

        deleteFrom(database, migrations);
        database.close();

        migrationCompleteCallback.onMigrationComplete();
    }

    private void migrateV1FilesToV2Location(List<Migration> migrations) {
        for (Migration migration : migrations) {
            Batch batch = migration.batch();
            List<FileSize> fileSizes = migration.fileSizes();
            List<String> originalFileLocations = migration.originalFileLocations();

            migrateV1FilesToV2Location(batch, fileSizes, originalFileLocations);
        }
    }

    private void migrateV1FilesToV2Location(Batch batch, List<FileSize> fileSizes, List<String> originalFileLocations) {
        for (int i = 0; i < originalFileLocations.size(); i++) {
            String originalFileLocation = originalFileLocations.get(i);
            FileSize actualFileSize = fileSizes.get(i);
            FileName newFileName = LiteFileName.from(batch, batch.getFileUrls().get(i));
            internalFilePersistence.create(newFileName, actualFileSize);

            FileInputStream inputStream = null;
            try {
                // open the v1 file
                inputStream = new FileInputStream(new File(originalFileLocation));
                byte[] bytes = new byte[BUFFER_SIZE];

                // read the v1 file
                int readLast = 0;
                while (readLast != -1) {
                    readLast = inputStream.read(bytes);
                    if (readLast != 0 && readLast != -1) {
                        // write the v1 file to the v2 location
                        internalFilePersistence.write(bytes, 0, readLast);
                        bytes = new byte[BUFFER_SIZE];
                    }
                }
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    internalFilePersistence.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Log.e(getClass().getSimpleName(), e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void migrateV1DataToV2Database(List<Migration> migrations) {
        for (Migration migration : migrations) {
            Batch batch = migration.batch();
            List<FileSize> fileSizes = migration.fileSizes();
            List<String> originalFileLocations = migration.originalFileLocations();

            migrateV1DataToV2Database(batch, fileSizes, originalFileLocations);
        }
    }

    private void migrateV1DataToV2Database(Batch batch, List<FileSize> fileSizes, List<String> originalFileLocations) {
        downloadsPersistence.startTransaction();

        DownloadBatchTitle downloadBatchTitle = new LiteDownloadBatchTitle(batch.getTitle());
        DownloadsBatchPersisted persistedBatch = new LiteDownloadsBatchPersisted(downloadBatchTitle, batch.getDownloadBatchId(), Status.DOWNLOADED);
        downloadsPersistence.persistBatch(persistedBatch);

        for (int i = 0; i < originalFileLocations.size(); i++) {
            String url = batch.getFileUrls().get(i);
            FileName fileName = LiteFileName.from(batch, url);
            FilePath filePath = FilePathCreator.create(fileName.name());
            DownloadFileId downloadFileId = DownloadFileId.from(batch);
            DownloadsFilePersisted persistedFile = new LiteDownloadsFilePersisted(
                    batch.getDownloadBatchId(),
                    downloadFileId,
                    fileName,
                    filePath,
                    fileSizes.get(i).totalSize(),
                    url,
                    FilePersistenceType.INTERNAL
            );
            downloadsPersistence.persistFile(persistedFile);
        }

        downloadsPersistence.transactionSuccess();
        downloadsPersistence.endTransaction();
    }

    private void deleteFrom(SqlDatabaseWrapper database, List<Migration> migrations) {
        for (Migration migration : migrations) {
            Batch batch = migration.batch();
            database.rawQuery(DELETE_QUERY, batch.getDownloadBatchId().stringValue());
        }
    }

}
