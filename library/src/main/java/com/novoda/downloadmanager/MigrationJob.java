package com.novoda.downloadmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status;

class MigrationJob implements Runnable {

    private static final String TAG = "V1 to V2 migrator";

    private static final int RANDOMLY_CHOSEN_BUFFER_SIZE_THAT_SEEMS_TO_WORK = 4096;
    private static final String TABLE_BATCHES = "batches";
    private static final String WHERE_CLAUSE_ID = "_id = ?";

    private final Context context;
    private final File databasePath;
    private final List<MigrationCallback> migrationCallbacks = new ArrayList<>();

    MigrationJob(Context context, File databasePath) {
        this.context = context;
        this.databasePath = databasePath;
    }

    void addCallback(MigrationCallback callback) {
        migrationCallbacks.add(callback);
    }

    public void run() {
        InternalMigrationStatus migrationStatus = new VersionOneToVersionTwoMigrationStatus(MigrationStatus.Status.DB_NOT_PRESENT);
        if (!databasePath.exists()) {
            onUpdate(migrationStatus);
            return;
        }

        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(databasePath.getAbsolutePath(), null, 0);
        SqlDatabaseWrapper database = new SqlDatabaseWrapper(sqLiteDatabase);

        PartialDownloadMigrationExtractor partialDownloadMigrationExtractor = new PartialDownloadMigrationExtractor(database);
        MigrationExtractor migrationExtractor = new MigrationExtractor(database);
        DownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);
        InternalFilePersistence internalFilePersistence = new InternalFilePersistence();
        internalFilePersistence.initialiseWith(context);
        LocalFilesDirectory localFilesDirectory = new AndroidLocalFilesDirectory(context);
        UnlinkedDataRemover unlinkedDataRemover = new UnlinkedDataRemover(downloadsPersistence, localFilesDirectory);

        unlinkedDataRemover.remove();
        migrationStatus.markAsExtracting();
        onUpdate(migrationStatus);

        Log.d(TAG, "about to extract migrations, time is " + System.nanoTime());

        migratePartialDownloads(database, partialDownloadMigrationExtractor, downloadsPersistence);
        migrateCompleteDownloads(migrationStatus, database, migrationExtractor, downloadsPersistence, internalFilePersistence);
    }

    private void onUpdate(MigrationStatus migrationStatus) {
        for (MigrationCallback migrationCallback : migrationCallbacks) {
            migrationCallback.onUpdate(migrationStatus);
        }
    }

    private void migratePartialDownloads(SqlDatabaseWrapper database,
                                         PartialDownloadMigrationExtractor partialDownloadMigrationExtractor,
                                         DownloadsPersistence downloadsPersistence) {
        List<Migration> partialMigrations = partialDownloadMigrationExtractor.extractMigrations();
        for (Migration partialMigration : partialMigrations) {
            downloadsPersistence.startTransaction();
            database.startTransaction();

            migrateV1DataToV2Database(downloadsPersistence, partialMigration);
            deleteFrom(database, partialMigration);

            downloadsPersistence.transactionSuccess();
            downloadsPersistence.endTransaction();
            database.setTransactionSuccessful();
            database.endTransaction();
        }
        Log.d(TAG, "partial migrations are all EXTRACTED, time is " + System.nanoTime());
    }

    private void migrateV1DataToV2Database(DownloadsPersistence downloadsPersistence, Migration migration) {
        Batch batch = migration.batch();

        DownloadBatchTitle downloadBatchTitle = new LiteDownloadBatchTitle(batch.getTitle());

        Status downloadBatchStatus = migration.hasDownloadedBatch() ? Status.DOWNLOADED : Status.QUEUED;

        DownloadsBatchPersisted persistedBatch = new LiteDownloadsBatchPersisted(downloadBatchTitle, batch.getDownloadBatchId(), downloadBatchStatus);
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
                    FilePersistenceType.INTERNAL,
                    0 // TODO: Get from migration.
            );
            downloadsPersistence.persistFile(persistedFile);
        }
    }

    // TODO: See https://github.com/novoda/download-manager/issues/270
    private void deleteFrom(SqlDatabaseWrapper database, Migration migration) {
        Batch batch = migration.batch();
        Log.d(TAG, "about to delete the batch: " + batch.getDownloadBatchId().stringValue() + ", time is " + System.nanoTime());
        database.delete(TABLE_BATCHES, WHERE_CLAUSE_ID, batch.getDownloadBatchId().stringValue());
        for (Migration.FileMetadata metadata : migration.getFileMetadata()) {
            if (hasValidFileLocation(metadata)) {
                File file = new File(metadata.originalFileLocation());
                boolean deleted = file.delete();
                String message = String.format("File or Directory: %s deleted: %s", file.getPath(), deleted);
                Log.d(getClass().getSimpleName(), message);
            }
        }
    }

    private boolean hasValidFileLocation(Migration.FileMetadata metadata) {
        return metadata.originalFileLocation() != null && !metadata.originalFileLocation().isEmpty();
    }

    private void migrateCompleteDownloads(InternalMigrationStatus migrationStatus,
                                          SqlDatabaseWrapper database,
                                          MigrationExtractor migrationExtractor,
                                          DownloadsPersistence downloadsPersistence,
                                          InternalFilePersistence internalFilePersistence) {
        List<Migration> migrations = migrationExtractor.extractMigrations();
        Log.d(TAG, "migrations are all EXTRACTED, time is " + System.nanoTime());

        migrationStatus.markAsMigrating();
        onUpdate(migrationStatus);
        Log.d(TAG, "about to migrate the files, time is " + System.nanoTime());

        for (int i = 0, size = migrations.size(); i < size; i++) {
            migrationStatus.update(i, size - 1);
            onUpdate(migrationStatus);

            Migration migration = migrations.get(i);
            downloadsPersistence.startTransaction();
            database.startTransaction();

            migrateV1FilesToV2Location(internalFilePersistence, migration);
            migrateV1DataToV2Database(downloadsPersistence, migration);
            deleteFrom(database, migration);

            downloadsPersistence.transactionSuccess();
            downloadsPersistence.endTransaction();
            database.setTransactionSuccessful();
            database.endTransaction();
        }

        Log.d(TAG, "all data migrations are COMMITTED, about to delete the old database, time is " + System.nanoTime());

        migrationStatus.markAsDeleting();
        onUpdate(migrationStatus);
        Log.d(TAG, "all traces of v1 are ERASED, time is " + System.nanoTime());
        database.close();

        database.deleteDatabase();
        migrationStatus.markAsComplete();
        onUpdate(migrationStatus);
    }

    private void migrateV1FilesToV2Location(InternalFilePersistence internalFilePersistence, Migration migration) {
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
            } finally {
                try {
                    internalFilePersistence.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Log.e(getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }

}
