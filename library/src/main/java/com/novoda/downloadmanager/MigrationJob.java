package com.novoda.downloadmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status;

class MigrationJob implements Runnable {

    private static final String TAG = "V1 to V2 migrator";

    private static final String TABLE_BATCHES = "batches";
    private static final String WHERE_CLAUSE_ID = "_id = ?";

    private final Context context;
    private final String jobIdentifier;
    private final File databasePath;
    private final List<MigrationCallback> migrationCallbacks = new ArrayList<>();

    MigrationJob(Context context, String jobIdentifier, File databasePath) {
        this.context = context;
        this.jobIdentifier = jobIdentifier;
        this.databasePath = databasePath;
    }

    void addCallback(MigrationCallback callback) {
        migrationCallbacks.add(callback);
    }

    public void run() {
        InternalMigrationStatus migrationStatus = new VersionOneToVersionTwoMigrationStatus(
                jobIdentifier,
                MigrationStatus.Status.DB_NOT_PRESENT,
                0,
                0,
                0
        );

        if (!databasePath.exists()) {
            onUpdate(migrationStatus);
            return;
        }

        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(databasePath.getAbsolutePath(), null, 0);
        SqlDatabaseWrapper database = new SqlDatabaseWrapper(sqLiteDatabase);

        FilePersistence filePersistence = FilePersistenceCreator.newInternalFilePersistenceCreator(context).create();
        filePersistence.initialiseWith(context);
        PartialDownloadMigrationExtractor partialDownloadMigrationExtractor = new PartialDownloadMigrationExtractor(database);
        MigrationExtractor migrationExtractor = new MigrationExtractor(database, filePersistence);
        DownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);
        LocalFilesDirectory localFilesDirectory = new AndroidLocalFilesDirectory(context);
        UnlinkedDataRemover unlinkedDataRemover = new UnlinkedDataRemover(downloadsPersistence, localFilesDirectory);

        unlinkedDataRemover.remove();
        migrationStatus.markAsExtracting();
        onUpdate(migrationStatus);

        Log.d(TAG, "about to extract migrations, time is " + System.nanoTime());

        String basePath = filePersistence.basePath().path();

        List<Migration> partialMigrations = partialDownloadMigrationExtractor.extractMigrations();
        List<Migration> completeMigrations = migrationExtractor.extractMigrations();

        int totalMigrations = partialMigrations.size() + completeMigrations.size();

        migrationStatus = new VersionOneToVersionTwoMigrationStatus(
                migrationStatus.migrationId(),
                migrationStatus.status(),
                migrationStatus.numberOfMigratedBatches(),
                totalMigrations,
                migrationStatus.percentageMigrated()
        );

        migratePartialDownloads(database, partialMigrations, downloadsPersistence, basePath);
        migrateCompleteDownloads(migrationStatus, database, completeMigrations, downloadsPersistence, basePath);
    }

    private void onUpdate(MigrationStatus migrationStatus) {
        MigrationStatus clonedMigrationStatus = new VersionOneToVersionTwoMigrationStatus(
                migrationStatus.migrationId(),
                migrationStatus.status(),
                migrationStatus.numberOfMigratedBatches(),
                migrationStatus.totalNumberOfBatchesToMigrate(),
                migrationStatus.percentageMigrated()
        );

        for (MigrationCallback migrationCallback : migrationCallbacks) {
            migrationCallback.onUpdate(clonedMigrationStatus);
        }
    }

    private void migratePartialDownloads(SqlDatabaseWrapper database,
                                         List<Migration> partialMigrations,
                                         DownloadsPersistence downloadsPersistence,
                                         String basePath) {
        for (Migration partialMigration : partialMigrations) {
            downloadsPersistence.startTransaction();
            database.startTransaction();

            migrateV1DataToV2Database(downloadsPersistence, partialMigration, basePath, false);
            deleteFrom(database, partialMigration);
            deleteFiles(partialMigration);

            downloadsPersistence.transactionSuccess();
            downloadsPersistence.endTransaction();
            database.setTransactionSuccessful();
            database.endTransaction();
        }
        Log.d(TAG, "partial migrations are all EXTRACTED, time is " + System.nanoTime());
    }

    private void migrateV1DataToV2Database(DownloadsPersistence downloadsPersistence,
                                           Migration migration,
                                           String basePath,
                                           boolean notificationSeen) {
        Batch batch = migration.batch();

        DownloadBatchId downloadBatchId = batch.downloadBatchId();
        DownloadBatchTitle downloadBatchTitle = new LiteDownloadBatchTitle(batch.title());
        Status downloadBatchStatus = batchStatusFrom(migration);
        long downloadedDateTimeInMillis = migration.downloadedDateTimeInMillis();

        DownloadsBatchPersisted persistedBatch = new LiteDownloadsBatchPersisted(
                downloadBatchTitle,
                downloadBatchId,
                downloadBatchStatus,
                downloadedDateTimeInMillis,
                notificationSeen
        );
        downloadsPersistence.persistBatch(persistedBatch);

        for (Migration.FileMetadata fileMetadata : migration.getFileMetadata()) {
            String url = fileMetadata.originalNetworkAddress();

            FilePath filePath = new LiteFilePath(fileMetadata.originalFileLocation());
            if (filePath.path() == null || filePath.path().isEmpty()) {
                filePath = FilePathCreator.create(basePath, FileNameExtractor.extractFrom(url).name());
            }
            FileName fileName = LiteFileName.from(batch, url);

            String rawDownloadFileId = rawFileIdFrom(batch, fileMetadata);
            DownloadFileId downloadFileId = DownloadFileIdCreator.createFrom(rawDownloadFileId);

            DownloadsFilePersisted persistedFile = new LiteDownloadsFilePersisted(
                    downloadBatchId,
                    downloadFileId,
                    fileName,
                    filePath,
                    fileMetadata.fileSize().totalSize(),
                    url,
                    FilePersistenceType.INTERNAL
            );
            downloadsPersistence.persistFile(persistedFile);
        }
    }

    private Status batchStatusFrom(Migration migration) {
        return migration.type() == Migration.Type.COMPLETE ? Status.DOWNLOADED : Status.QUEUED;
    }

    private String rawFileIdFrom(Batch batch, Migration.FileMetadata fileMetadata) {
        if (fileMetadata.fileId() == null || fileMetadata.fileId().isEmpty()) {
            return batch.title() + System.nanoTime();
        } else {
            return fileMetadata.fileId();
        }
    }

    // TODO: See https://github.com/novoda/download-manager/issues/270
    private void deleteFiles(Migration migration) {
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
                                          List<Migration> completeMigrations,
                                          DownloadsPersistence downloadsPersistence,
                                          String basePath) {
        Log.d(TAG, "migrations are all EXTRACTED, time is " + System.nanoTime());

        migrationStatus.markAsMigrating();
        onUpdate(migrationStatus);
        Log.d(TAG, "about to migrate the files, time is " + System.nanoTime());

        for (int i = 0, size = completeMigrations.size(); i < size; i++) {
            migrationStatus.update(i, size);
            onUpdate(migrationStatus);

            Migration migration = completeMigrations.get(i);
            downloadsPersistence.startTransaction();
            database.startTransaction();

            migrateV1DataToV2Database(downloadsPersistence, migration, basePath, true);
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

    private void deleteFrom(SqlDatabaseWrapper database, Migration migration) {
        Batch batch = migration.batch();
        Log.d(TAG, "about to delete the batch: " + batch.downloadBatchId().rawId() + ", time is " + System.nanoTime());
        database.delete(TABLE_BATCHES, WHERE_CLAUSE_ID, batch.downloadBatchId().rawId());
    }

}
