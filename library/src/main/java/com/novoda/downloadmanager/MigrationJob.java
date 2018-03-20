package com.novoda.downloadmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status;

class MigrationJob implements Runnable {

    private static final String TABLE_BATCHES = "batches";
    private static final String WHERE_CLAUSE_ID = "_id = ?";
    private static final int NO_COMPLETED_MIGRATIONS = 0;
    private static final int NO_MIGRATIONS = 0;

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
        if (!databasePath.exists()) {
            InternalMigrationStatus migrationStatus = new VersionOneToVersionTwoMigrationStatus(
                    jobIdentifier,
                    MigrationStatus.Status.DB_NOT_PRESENT,
                    NO_COMPLETED_MIGRATIONS,
                    NO_MIGRATIONS
            );

            onUpdate(migrationStatus);
            return;
        }

        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(databasePath.getAbsolutePath(), null, 0);
        SqlDatabaseWrapper database = new SqlDatabaseWrapper(sqLiteDatabase);

        FilePersistence filePersistence = FilePersistenceCreator.newInternalFilePersistenceCreator(context).create();
        String basePath = filePersistence.basePath().path();
        filePersistence.initialiseWith(context);
        PartialDownloadMigrationExtractor partialDownloadMigrationExtractor = new PartialDownloadMigrationExtractor(database);
        MigrationExtractor migrationExtractor = new MigrationExtractor(database, filePersistence);
        List<Migration> partialMigrations = partialDownloadMigrationExtractor.extractMigrations();
        List<Migration> completeMigrations = migrationExtractor.extractMigrations();
        DownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);
        LocalFilesDirectory localFilesDirectory = new AndroidLocalFilesDirectory(context);
        UnlinkedDataRemover unlinkedDataRemover = new UnlinkedDataRemover(downloadsPersistence, localFilesDirectory);

        int totalNumberOfMigrations = partialMigrations.size() + completeMigrations.size();
        InternalMigrationStatus migrationStatus = new VersionOneToVersionTwoMigrationStatus(
                jobIdentifier,
                MigrationStatus.Status.DB_NOT_PRESENT,
                NO_COMPLETED_MIGRATIONS,
                totalNumberOfMigrations
        );

        unlinkedDataRemover.remove();
        migrationStatus.markAsExtracting();
        onUpdate(migrationStatus);

        migrationStatus.markAsMigrating();
        onUpdate(migrationStatus);

        migratePartialDownloads(migrationStatus, database, partialMigrations, downloadsPersistence, basePath);
        migrateCompleteDownloads(migrationStatus, database, completeMigrations, downloadsPersistence, basePath);
        deleteVersionOneDatabase(migrationStatus, database);

        migrationStatus.markAsComplete();
        onUpdate(migrationStatus);
    }

    private void onUpdate(InternalMigrationStatus migrationStatus) {
        for (MigrationCallback migrationCallback : migrationCallbacks) {
            migrationCallback.onUpdate(migrationStatus.copy());
        }
    }

    private void migratePartialDownloads(InternalMigrationStatus migrationStatus,
                                         SqlDatabaseWrapper database,
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
            migrationStatus.onSingleBatchMigrated();
            onUpdate(migrationStatus);
        }
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
                if (!deleted) {
                    String message = String.format("Could not delete File or Directory: %s", file.getPath());
                    Logger.e(getClass().getSimpleName(), message);
                }
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
        for (Migration completeMigration : completeMigrations) {
            downloadsPersistence.startTransaction();
            database.startTransaction();

            migrateV1DataToV2Database(downloadsPersistence, completeMigration, basePath, true);
            deleteFrom(database, completeMigration);

            downloadsPersistence.transactionSuccess();
            downloadsPersistence.endTransaction();
            database.setTransactionSuccessful();
            database.endTransaction();
            migrationStatus.onSingleBatchMigrated();
            onUpdate(migrationStatus);
        }
    }

    private void deleteFrom(SqlDatabaseWrapper database, Migration migration) {
        Batch batch = migration.batch();
        database.delete(TABLE_BATCHES, WHERE_CLAUSE_ID, batch.downloadBatchId().rawId());
    }

    private void deleteVersionOneDatabase(InternalMigrationStatus migrationStatus, SqlDatabaseWrapper database) {
        migrationStatus.markAsDeleting();
        onUpdate(migrationStatus);

        database.close();
        database.deleteDatabase();
    }

}
