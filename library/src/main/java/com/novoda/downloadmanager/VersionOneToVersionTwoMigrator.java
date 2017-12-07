package com.novoda.downloadmanager;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status;

class VersionOneToVersionTwoMigrator implements Migrator {

    private static final int RANDOMLY_CHOSEN_BUFFER_SIZE_THAT_SEEMS_TO_WORK = 4096;
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM batches WHERE _id = ?";

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
            for (Migration.FileMetadata fileMetadata : migration.getFileMetadata()) {
                FileName newFileName = LiteFileName.from(batch, fileMetadata.uri());
                internalFilePersistence.create(newFileName, fileMetadata.fileSize());
                FileInputStream inputStream = null;
                try {
                    // open the v1 file
                    inputStream = new FileInputStream(new File(fileMetadata.originalFileLocation()));
                    byte[] bytes = new byte[RANDOMLY_CHOSEN_BUFFER_SIZE_THAT_SEEMS_TO_WORK];

                    // read the v1 file
                    int readLast = 0;
                    while (readLast != -1) {
                        readLast = inputStream.read(bytes);
                        if (readLast != 0 && readLast != -1) {
                            // write the v1 file to the v2 location
                            internalFilePersistence.write(bytes, 0, readLast);
                            bytes = new byte[RANDOMLY_CHOSEN_BUFFER_SIZE_THAT_SEEMS_TO_WORK];
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
    }

    private void migrateV1DataToV2Database(List<Migration> migrations) {
        for (Migration migration : migrations) {
            Batch batch = migration.batch();

            downloadsPersistence.startTransaction();

            DownloadBatchTitle downloadBatchTitle = new LiteDownloadBatchTitle(batch.getTitle());
            DownloadsBatchPersisted persistedBatch = new LiteDownloadsBatchPersisted(downloadBatchTitle, batch.getDownloadBatchId(), Status.DOWNLOADED);
            downloadsPersistence.persistBatch(persistedBatch);

            for (Migration.FileMetadata fileMetadata : migration.getFileMetadata()) {
                String url = fileMetadata.uri();

                FileName fileName = LiteFileName.from(batch, url);
                FilePath filePath = FilePathCreator.create(fileName.name());
                DownloadFileId downloadFileId = DownloadFileId.from(batch);
                DownloadsFilePersisted persistedFile = new LiteDownloadsFilePersisted(
                        batch.getDownloadBatchId(),
                        downloadFileId,
                        fileName,
                        filePath,
                        fileMetadata.fileSize().totalSize(),
                        url,
                        FilePersistenceType.INTERNAL
                );
                downloadsPersistence.persistFile(persistedFile);
            }

            downloadsPersistence.transactionSuccess();
            downloadsPersistence.endTransaction();
        }
    }

    private void deleteFrom(SqlDatabaseWrapper database, List<Migration> migrations) {
        for (Migration migration : migrations) {
            Batch batch = migration.batch();
            database.rawQuery(DELETE_BY_ID_QUERY, batch.getDownloadBatchId().stringValue());
        }
    }

}
