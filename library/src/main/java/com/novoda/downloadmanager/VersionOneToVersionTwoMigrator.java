package com.novoda.downloadmanager;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

class VersionOneToVersionTwoMigrator implements Migrator {

    private static final int BUFFER_SIZE = 8 * 512;

    private final MigrationExtractor migrationExtractor;
    private final RoomDownloadsPersistence downloadsPersistence;
    private final InternalFilePersistence internalFilePersistence;
    private final File databasePath;
    private final Callback migrationCompleteCallback;

    VersionOneToVersionTwoMigrator(MigrationExtractor migrationExtractor,
                                   RoomDownloadsPersistence downloadsPersistence,
                                   InternalFilePersistence internalFilePersistence,
                                   File databasePath,
                                   Callback migrationCompleteCallback) {
        this.migrationExtractor = migrationExtractor;
        this.downloadsPersistence = downloadsPersistence;
        this.internalFilePersistence = internalFilePersistence;
        this.databasePath = databasePath;
        this.migrationCompleteCallback = migrationCompleteCallback;
    }

    @Override
    public void migrate() {
        if (checkV1DatabaseExists()) {
            SQLiteDatabase database = SQLiteDatabase.openDatabase(databasePath.getAbsolutePath(), null, 0);
            DatabaseWrapper databaseWrapper = new DatabaseWrapper(database);
            List<Migration> migrations = migrationExtractor.extractMigrationsFrom(databaseWrapper);

            migrateV1FilesToV2Location(migrations);
            migrateV1DataToV2Database(migrations);

            deleteFrom(databaseWrapper, migrations);
            databaseWrapper.close();

            migrationCompleteCallback.onMigrationComplete();
        } else {
            Log.d("MainActivity", "downloads.db doesn't exist!");
        }
    }

    private boolean checkV1DatabaseExists() {
        return databasePath.exists();
    }

    private void migrateV1FilesToV2Location(List<Migration> migrations) {
        for (Migration migration : migrations) {
            migrateV1FilesToV2Location(migration);
        }
    }

    private void migrateV1FilesToV2Location(Migration migration) {
        Batch batch = migration.batch();

        for (int i = 0; i < migration.originalFileLocations().size(); i++) {
            // initialise the InternalFilePersistence
            String originalFileLocation = migration.originalFileLocations().get(i);
            FileSize actualFileSize = migration.fileSizes().get(i);
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
            migrateV1DataToV2Database(migration);
        }
    }

    private void migrateV1DataToV2Database(Migration migration) {
        Batch batch = migration.batch();
        downloadsPersistence.startTransaction();

        DownloadBatchTitle downloadBatchTitle = new LiteDownloadBatchTitle(batch.getTitle());
        DownloadsBatchPersisted persistedBatch = new LiteDownloadsBatchPersisted(downloadBatchTitle, batch.getDownloadBatchId(), DownloadBatchStatus.Status.DOWNLOADED);
        downloadsPersistence.persistBatch(persistedBatch);

        for (int i = 0; i < migration.originalFileLocations().size(); i++) {
            String url = batch.getFileUrls().get(i);
            FileName fileName = LiteFileName.from(batch, url);
            FilePath filePath = FilePathCreator.create(fileName.name());
            DownloadFileId downloadFileId = DownloadFileId.from(batch);
            DownloadsFilePersisted persistedFile = new LiteDownloadsFilePersisted(
                    batch.getDownloadBatchId(),
                    downloadFileId,
                    fileName,
                    filePath,
                    migration.fileSizes().get(i).totalSize(),
                    url,
                    FilePersistenceType.INTERNAL
            );
            downloadsPersistence.persistFile(persistedFile);
        }

        downloadsPersistence.transactionSuccess();
        downloadsPersistence.endTransaction();
    }

    private void deleteFrom(DatabaseWrapper database, List<Migration> migrations) {
        for (Migration migration : migrations) {
            String query = "DELETE FROM batches WHERE _id = ?";
            Batch batch = migration.batch();
            database.rawQuery(query, batch.getDownloadBatchId().stringValue());
        }
    }

}
