package com.novoda.downloadmanager.demo;

import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadsBatchPersisted;
import com.novoda.downloadmanager.DownloadsFilePersisted;
import com.novoda.downloadmanager.DownloadsPersistence;
import com.novoda.downloadmanager.Logger;

import java.util.Collections;
import java.util.List;

public class CustomDownloadsPersistence implements DownloadsPersistence {

    @Override
    public void startTransaction() {
        Logger.v("Start Transaction");
    }

    @Override
    public void endTransaction() {
        Logger.v("End Transaction");
    }

    @Override
    public void transactionSuccess() {
        Logger.v("Transaction success");
    }

    @Override
    public void persistBatch(DownloadsBatchPersisted batchPersisted) {
        Logger.v("Persist batch id: " + batchPersisted.downloadBatchId() + ", status: " + batchPersisted.downloadBatchStatus());
    }

    @Override
    public List<DownloadsBatchPersisted> loadBatches() {
        Logger.v("Load batches");
        return Collections.emptyList();
    }

    @Override
    public void persistFile(DownloadsFilePersisted filePersisted) {
        Logger.v("Persist file id: " + filePersisted.downloadFileId());
    }

    @Override
    public List<DownloadsFilePersisted> loadAllFiles() {
        Logger.v("Load all files");
        return Collections.emptyList();
    }

    @Override
    public List<DownloadsFilePersisted> loadFiles(DownloadBatchId batchId) {
        Logger.v("Load files for batch id: " + batchId);
        return Collections.emptyList();
    }

    @Override
    public boolean delete(DownloadBatchId downloadBatchId) {
        Logger.v("Delete batch id: " + downloadBatchId.rawId());
        return true;
    }

    @Override
    public boolean update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
        Logger.v("update batch id: " + downloadBatchId.rawId() + " with status: " + status);
        return true;
    }

    @Override
    public boolean update(DownloadBatchId downloadBatchId, boolean notificationSeen) {
        Logger.v("update batch id: " + downloadBatchId.rawId() + " with notificationSeen: " + notificationSeen);
        return true;
    }
}
