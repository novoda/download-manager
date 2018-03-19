package com.novoda.downloadmanager.demo;

import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadsBatchPersisted;
import com.novoda.downloadmanager.DownloadsFilePersisted;
import com.novoda.downloadmanager.DownloadsPersistence;
import com.novoda.notils.logger.simple.Log;

import java.util.Collections;
import java.util.List;

public class CustomDownloadsPersistence implements DownloadsPersistence {

    @Override
    public void startTransaction() {
        Log.v("Start Transaction");
    }

    @Override
    public void endTransaction() {
        Log.v("End Transaction");
    }

    @Override
    public void transactionSuccess() {
        Log.v("Transaction success");
    }

    @Override
    public void persistBatch(DownloadsBatchPersisted batchPersisted) {
        Log.v("Persist batch id: " + batchPersisted.downloadBatchId() + ", status: " + batchPersisted.downloadBatchStatus());
    }

    @Override
    public List<DownloadsBatchPersisted> loadBatches() {
        Log.v("Load batches");
        return Collections.emptyList();
    }

    @Override
    public void persistFile(DownloadsFilePersisted filePersisted) {
        Log.v("Persist file id: " + filePersisted.downloadFileId());
    }

    @Override
    public List<DownloadsFilePersisted> loadAllFiles() {
        Log.v("Load all files");
        return Collections.emptyList();
    }

    @Override
    public List<DownloadsFilePersisted> loadFiles(DownloadBatchId batchId) {
        Log.v("Load files for batch id: " + batchId);
        return Collections.emptyList();
    }

    @Override
    public boolean delete(DownloadBatchId downloadBatchId) {
        Log.v("Delete batch id: " + downloadBatchId.rawId());
        return true;
    }

    @Override
    public boolean update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
        Log.v("update batch id: " + downloadBatchId.rawId() + " with status: " + status);
        return true;
    }

    @Override
    public boolean update(DownloadBatchId downloadBatchId, boolean notificationSeen) {
        Log.v("update batch id: " + downloadBatchId.rawId() + " with notificationSeen: " + notificationSeen);
        return true;
    }
}
