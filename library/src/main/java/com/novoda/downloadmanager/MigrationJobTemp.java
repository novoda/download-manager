package com.novoda.downloadmanager;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MigrationJobTemp {

    private static final int NO_COMPLETED_MIGRATIONS = 0;
    private static final int RANDOMLY_CHOSEN_BUFFER_SIZE_THAT_SEEMS_TO_WORK = 4096;
    private static final float TEN_PERCENT = 0.1f;

    private final Context context;
    private final String jobIdentifier;
    private final List<MigrationCallback> migrationCallbacks = new ArrayList<>();

    MigrationJobTemp(Context context, String jobIdentifier) {
        this.context = context;
        this.jobIdentifier = jobIdentifier;
    }

    void addCallback(MigrationCallback callback) {
        migrationCallbacks.add(callback);
    }

    public void migrate(List<Migration> partialMigrations, List<Migration> completeMigrations) {
        FilePersistenceCreator filePersistenceCreator = FilePersistenceCreator.newInternalFilePersistenceCreator(context);
        StorageRequirementsRule storageRequirementsRule = StorageRequirementsRule.withPercentageOfStorageRemaining(TEN_PERCENT);
        filePersistenceCreator.withStorageRequirementsRule(storageRequirementsRule);
        FilePersistence filePersistence = filePersistenceCreator.create();

        DownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);

        int totalNumberOfMigrations = partialMigrations.size() + completeMigrations.size();
        InternalMigrationStatus migrationStatus = new VersionOneToVersionTwoMigrationStatus(
                jobIdentifier,
                MigrationStatus.Status.DB_NOT_PRESENT,
                NO_COMPLETED_MIGRATIONS,
                totalNumberOfMigrations
        );

        migrationStatus.markAsExtracting();
        onUpdate(migrationStatus);

        migrationStatus.markAsMigrating();
        onUpdate(migrationStatus);

        migrateCompleteDownloads(migrationStatus, completeMigrations, downloadsPersistence, filePersistence);
        migratePartialDownloads(migrationStatus, partialMigrations, downloadsPersistence);

        migrationStatus.markAsComplete();
        onUpdate(migrationStatus);
    }

    private void onUpdate(InternalMigrationStatus migrationStatus) {
        for (MigrationCallback migrationCallback : migrationCallbacks) {
            migrationCallback.onUpdate(migrationStatus.copy());
        }
    }

    private void migrateCompleteDownloads(InternalMigrationStatus migrationStatus,
                                          List<Migration> completeMigrations,
                                          DownloadsPersistence downloadsPersistence,
                                          FilePersistence filePersistence) {
        for (Migration completeMigration : completeMigrations) {
            downloadsPersistence.startTransaction();

            migrateV1FilesToV2Location(filePersistence, completeMigration);
            migrateV1DataToV2Database(downloadsPersistence, completeMigration, true);

            downloadsPersistence.transactionSuccess();
            downloadsPersistence.endTransaction();
            migrationStatus.onSingleBatchMigrated();
            onUpdate(migrationStatus);
        }
    }

    private void migrateV1FilesToV2Location(FilePersistence filePersistence, Migration migration) {
        for (Migration.FileMetadata fileMetadata : migration.getFileMetadata()) {
            filePersistence.create(fileMetadata.newFileLocation(), fileMetadata.fileSize());
            FileInputStream inputStream = null;
            try {
                // open the v1 file
                inputStream = new FileInputStream(new File(fileMetadata.originalFileLocation().path()));
                byte[] bytes = new byte[RANDOMLY_CHOSEN_BUFFER_SIZE_THAT_SEEMS_TO_WORK];

                // read the v1 file
                int readLast = 0;
                while (readLast != -1) {
                    readLast = inputStream.read(bytes);
                    if (readLast != 0 && readLast != -1) {
                        // write the v1 file to the v2 location
                        filePersistence.write(bytes, 0, readLast);
                    }
                }
            } catch (IOException e) {
                Logger.e(e.getMessage());
            } finally {
                try {
                    filePersistence.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Logger.e(e.getMessage());
                }
            }
        }
    }

    private void migrateV1DataToV2Database(DownloadsPersistence downloadsPersistence,
                                           Migration migration,
                                           boolean notificationSeen) {
        Batch batch = migration.batch();

        DownloadBatchId downloadBatchId = batch.downloadBatchId();
        DownloadBatchTitle downloadBatchTitle = new LiteDownloadBatchTitle(batch.title());
        DownloadBatchStatus.Status downloadBatchStatus = batchStatusFrom(migration);
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

            String rawDownloadFileId = rawFileIdFrom(batch, fileMetadata);
            DownloadFileId downloadFileId = DownloadFileIdCreator.createFrom(rawDownloadFileId);

            DownloadsFilePersisted persistedFile = new LiteDownloadsFilePersisted(
                    downloadBatchId,
                    downloadFileId,
                    fileMetadata.newFileLocation(),
                    fileMetadata.fileSize().totalSize(),
                    url,
                    FilePersistenceType.INTERNAL
            );
            downloadsPersistence.persistFile(persistedFile);
        }
    }

    private DownloadBatchStatus.Status batchStatusFrom(Migration migration) {
        return migration.type() == Migration.Type.COMPLETE ? DownloadBatchStatus.Status.DOWNLOADED : DownloadBatchStatus.Status.QUEUED;
    }

    private String rawFileIdFrom(Batch batch, Migration.FileMetadata fileMetadata) {
        if (fileMetadata.fileId() == null || fileMetadata.fileId().isEmpty()) {
            return batch.title() + System.nanoTime();
        } else {
            return fileMetadata.fileId();
        }
    }

    private void migratePartialDownloads(InternalMigrationStatus migrationStatus,
                                         List<Migration> partialMigrations,
                                         DownloadsPersistence downloadsPersistence) {
        for (Migration partialMigration : partialMigrations) {
            downloadsPersistence.startTransaction();

            migrateV1DataToV2Database(downloadsPersistence, partialMigration, false);

            downloadsPersistence.transactionSuccess();
            downloadsPersistence.endTransaction();
            migrationStatus.onSingleBatchMigrated();
            onUpdate(migrationStatus);
        }
    }

}
