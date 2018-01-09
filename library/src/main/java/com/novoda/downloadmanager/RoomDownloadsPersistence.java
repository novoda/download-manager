package com.novoda.downloadmanager;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

final class RoomDownloadsPersistence implements DownloadsPersistence {

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
    public void persistBatch(final DownloadsBatchPersisted batchPersisted) {
        RoomBatch roomBatch = new RoomBatch();
        roomBatch.id = batchPersisted.downloadBatchId().stringValue();
        roomBatch.status = batchPersisted.downloadBatchStatus().toRawValue();
        roomBatch.title = batchPersisted.downloadBatchTitle().asString();

        database.roomBatchDao().insert(roomBatch);
    }

    @Override
    public List<DownloadsBatchPersisted> loadBatches() {
        List<RoomBatch> roomBatches = database.roomBatchDao().loadAll();

        List<DownloadsBatchPersisted> batchPersistedList = new ArrayList<>(roomBatches.size());
        for (RoomBatch roomBatch : roomBatches) {
            DownloadsBatchPersisted batchPersisted = new LiteDownloadsBatchPersisted(
                    DownloadBatchTitleCreator.createFrom(roomBatch.title),
                    DownloadBatchIdCreator.createFrom(roomBatch.id),
                    DownloadBatchStatus.Status.from(roomBatch.status)
            );
            batchPersistedList.add(batchPersisted);
        }

        return batchPersistedList;
    }

    @Override
    public void persistFile(DownloadsFilePersisted filePersisted) {
        RoomFile roomFile = new RoomFile();
        roomFile.totalSize = filePersisted.totalFileSize();
        roomFile.batchId = filePersisted.downloadBatchId().stringValue();
        roomFile.url = filePersisted.url();
        roomFile.name = filePersisted.fileName().name();
        roomFile.path = filePersisted.filePath().path();
        roomFile.id = filePersisted.downloadFileId().toRawId();
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
        List<RoomFile> roomFiles = database.roomFileDao().loadAllFilesFor(downloadBatchId.stringValue());
        return getDownloadsFilePersisted(roomFiles);
    }

    private List<DownloadsFilePersisted> getDownloadsFilePersisted(List<RoomFile> roomFiles) {
        List<DownloadsFilePersisted> filePersistedList = new ArrayList<>(roomFiles.size());
        for (RoomFile roomFile : roomFiles) {
            DownloadsFilePersisted filePersisted = new LiteDownloadsFilePersisted(
                    DownloadBatchIdCreator.createFrom(roomFile.batchId),
                    DownloadFileIdCreator.createFrom(roomFile.id),
                    LiteFileName.from(roomFile.name),
                    FilePathCreator.create(roomFile.path),
                    roomFile.totalSize,
                    roomFile.url,
                    FilePersistenceType.from(roomFile.persistenceType)
            );
            filePersistedList.add(filePersisted);
        }

        return filePersistedList;
    }

    @Override
    public void delete(DownloadBatchId downloadBatchId) {
        RoomBatch roomBatch = database.roomBatchDao().load(downloadBatchId.stringValue());
        database.roomBatchDao().delete(roomBatch);
    }

    @Override
    public void update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
        RoomBatch roomBatch = database.roomBatchDao().load(downloadBatchId.stringValue());
        roomBatch.status = status.toRawValue();
        database.roomBatchDao().update(roomBatch);
    }
}
