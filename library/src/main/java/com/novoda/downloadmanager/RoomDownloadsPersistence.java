package com.novoda.downloadmanager;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class RoomDownloadsPersistence implements DownloadsPersistence {

    private static final boolean NOTIFICATION_SEEN = true;
    private final RoomAppDatabase database;

    static RoomDownloadsPersistence newInstance(Context context) {
        RoomAppDatabase database = RoomAppDatabase.obtainInstance(context);
        return new RoomDownloadsPersistence(database);
    }

    private RoomDownloadsPersistence(RoomAppDatabase database) {
        this.database = database;
    }

    @Override
    public void startTransaction() {
        database.beginTransaction();
    }

    @Override
    public void endTransaction() {
        database.endTransaction();
    }

    @Override
    public void transactionSuccess() {
        database.setTransactionSuccessful();
    }

    @Override
    public void persistBatch(DownloadsBatchPersisted batchPersisted) {
        RoomBatch roomBatch = new RoomBatch();
        roomBatch.id = batchPersisted.downloadBatchId().rawId();
        roomBatch.status = batchPersisted.downloadBatchStatus().toRawValue();
        roomBatch.title = batchPersisted.downloadBatchTitle().asString();
        roomBatch.downloadedDateTimeInMillis = batchPersisted.downloadedDateTimeInMillis();
        roomBatch.notificationSeen = batchPersisted.notificationSeen();

        database.roomBatchDao().insert(roomBatch);
    }

    @Override
    public List<DownloadsBatchPersisted> loadBatches() {
        List<RoomBatch> roomBatches = database.roomBatchDao().loadAll();

        List<DownloadsBatchPersisted> batchPersistedList = new ArrayList<>(roomBatches.size());
        for (RoomBatch roomBatch : roomBatches) {
            DownloadsBatchPersisted batchPersisted = new LiteDownloadsBatchPersisted(
                    DownloadBatchTitleCreator.createFrom(roomBatch.title),
                    DownloadBatchIdCreator.createSanitizedFrom(roomBatch.id),
                    DownloadBatchStatus.Status.from(roomBatch.status),
                    roomBatch.downloadedDateTimeInMillis,
                    roomBatch.notificationSeen
            );
            batchPersistedList.add(batchPersisted);
        }

        return batchPersistedList;
    }

    @Override
    public void persistFile(DownloadsFilePersisted filePersisted) {
        RoomFile roomFile = new RoomFile();
        roomFile.totalSize = filePersisted.totalFileSize();
        roomFile.batchId = filePersisted.downloadBatchId().rawId();
        roomFile.url = filePersisted.url();
        roomFile.path = filePersisted.filePath().path();
        roomFile.fileId = filePersisted.downloadFileId().rawId();
        roomFile.persistenceType = filePersisted.filePersistenceType().toRawValue();

        database.roomFileDao().insert(roomFile);
    }

    @Override
    public List<DownloadsFilePersisted> loadAllFiles() {
        List<RoomFile> roomFiles = database.roomFileDao().loadAllFiles();
        return getDownloadsFilePersisted(roomFiles);
    }

    @Override
    public List<DownloadsFilePersisted> loadFiles(DownloadBatchId downloadBatchId) {
        List<RoomFile> roomFiles = database.roomFileDao().loadAllFilesFor(downloadBatchId.rawId());
        return getDownloadsFilePersisted(roomFiles);
    }

    private List<DownloadsFilePersisted> getDownloadsFilePersisted(List<RoomFile> roomFiles) {
        List<DownloadsFilePersisted> filePersistedList = new ArrayList<>(roomFiles.size());
        for (RoomFile roomFile : roomFiles) {
            DownloadsFilePersisted filePersisted = new LiteDownloadsFilePersisted(
                    DownloadBatchIdCreator.createSanitizedFrom(roomFile.batchId),
                    DownloadFileIdCreator.createFrom(roomFile.fileId),
                    new LiteFilePath(roomFile.path),
                    roomFile.totalSize,
                    roomFile.url,
                    FilePersistenceType.from(roomFile.persistenceType)
            );
            filePersistedList.add(filePersisted);
        }

        return filePersistedList;
    }

    @Override
    public boolean delete(DownloadBatchId downloadBatchId) {
        RoomBatch roomBatch = database.roomBatchDao().load(downloadBatchId.rawId());
        if (roomBatch == null) {
            return false;
        }

        database.roomBatchDao().delete(roomBatch);
        return true;
    }

    @Override
    public boolean update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
        RoomBatch roomBatch = database.roomBatchDao().load(downloadBatchId.rawId());
        if (roomBatch == null) {
            return false;
        }

        roomBatch.status = status.toRawValue();
        database.roomBatchDao().update(roomBatch);
        return true;
    }

    @Override
    public boolean update(DownloadBatchId downloadBatchId, boolean notificationSeen) {
        RoomBatch roomBatch = database.roomBatchDao().load(downloadBatchId.rawId());
        if (roomBatch == null) {
            return false;
        }
        roomBatch.notificationSeen = notificationSeen;
        database.roomBatchDao().update(roomBatch);
        return true;
    }

    @Override
    public void persistCompletedBatch(CompletedDownloadBatch completedDownloadBatch) {
        DownloadBatchId downloadBatchId = completedDownloadBatch.downloadBatchId();
        DownloadBatchTitle downloadBatchTitle = completedDownloadBatch.downloadBatchTitle();
        DownloadBatchStatus.Status downloadBatchStatus = DownloadBatchStatus.Status.DOWNLOADED;
        long downloadedDateTimeInMillis = completedDownloadBatch.downloadedDateTimeInMillis();

        DownloadsBatchPersisted persistedBatch = new LiteDownloadsBatchPersisted(
                downloadBatchTitle,
                downloadBatchId,
                downloadBatchStatus,
                downloadedDateTimeInMillis,
                NOTIFICATION_SEEN
        );
        persistBatch(persistedBatch);

        for (CompletedDownloadBatch.CompletedDownloadFile completedDownloadFile : completedDownloadBatch.completedDownloadFiles()) {
            String url = completedDownloadFile.originalNetworkAddress();

            String rawDownloadFileId = rawFileIdFrom(completedDownloadBatch.downloadBatchTitle(), completedDownloadFile);
            DownloadFileId downloadFileId = DownloadFileIdCreator.createFrom(rawDownloadFileId);

            DownloadsFilePersisted persistedFile = new LiteDownloadsFilePersisted(
                    downloadBatchId,
                    downloadFileId,
                    new LiteFilePath(completedDownloadFile.newFileLocation()),
                    completedDownloadFile.fileSize().totalSize(),
                    url,
                    FilePersistenceType.PATH
            );
            persistFile(persistedFile);
        }
    }

    private String rawFileIdFrom(DownloadBatchTitle batch, CompletedDownloadBatch.CompletedDownloadFile completedDownloadFile) {
        if (completedDownloadFile.fileId() == null || completedDownloadFile.fileId().isEmpty()) {
            return batch.asString() + UUID.randomUUID();
        } else {
            return completedDownloadFile.fileId();
        }
    }
}
