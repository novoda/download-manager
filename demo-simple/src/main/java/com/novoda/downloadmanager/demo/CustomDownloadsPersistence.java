package com.novoda.downloadmanager.demo;

import android.util.Log;

import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadsBatchPersisted;
import com.novoda.downloadmanager.DownloadsFilePersisted;
import com.novoda.downloadmanager.DownloadsPersistence;

import java.util.Collections;
import java.util.List;

public class CustomDownloadsPersistence implements DownloadsPersistence {

    private static final String TAG = CustomDownloadsPersistence.class.getSimpleName();

    @Override
    public void startTransaction() {
        Log.v(TAG, "Start Transaction");
    }

    @Override
    public void endTransaction() {
        Log.v(TAG, "End Transaction");
    }

    @Override
    public void transactionSuccess() {
        Log.v(TAG, "Transaction success");
    }

    @Override
    public void persistBatch(DownloadsBatchPersisted batchPersisted) {
        Log.v(TAG, "Persist batch id: " + batchPersisted.downloadBatchId() + ", status: " + batchPersisted.downloadBatchStatus());
    }

    @Override
    public List<DownloadsBatchPersisted> loadBatches() {
        Log.v(TAG, "Load batches");
        return Collections.emptyList();
    }

    @Override
    public void persistFile(DownloadsFilePersisted filePersisted) {
        Log.v(TAG, "Persist file id: " + filePersisted.downloadFileId());
    }

    @Override
    public List<DownloadsFilePersisted> loadAllFiles() {
        Log.v(TAG, "Load all files");
        return Collections.emptyList();
    }

    @Override
    public List<DownloadsFilePersisted> loadFiles(DownloadBatchId batchId) {
        Log.v(TAG, "Load files for batch id: " + batchId);
        return Collections.emptyList();
    }

    @Override
    public boolean delete(DownloadBatchId downloadBatchId) {
        Log.v(TAG, "Delete batch id: " + downloadBatchId.rawId());
        return true;
    }

    @Override
    public boolean update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
        Log.v(TAG, "update batch id: " + downloadBatchId.rawId() + " with status: " + status);
        return true;
    }

    @Override
    public boolean update(DownloadBatchId downloadBatchId, boolean notificationSeen) {
        Log.v(TAG, "update batch id: " + downloadBatchId.rawId() + " with notificationSeen: " + notificationSeen);
        return true;
    }
}
