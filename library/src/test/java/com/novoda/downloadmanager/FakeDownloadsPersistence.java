package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class FakeDownloadsPersistence implements DownloadsPersistence {

    private final Map<DownloadsBatchPersisted, List<DownloadsFilePersisted>> filesByBatches;

    FakeDownloadsPersistence(Map<DownloadsBatchPersisted, List<DownloadsFilePersisted>> filesByBatches) {
        this.filesByBatches = filesByBatches;
    }

    @Override
    public void startTransaction() {
        // no-op.
    }

    @Override
    public void endTransaction() {
        // no-op.
    }

    @Override
    public void transactionSuccess() {
        // no-op.
    }

    @Override
    public void persistBatch(DownloadsBatchPersisted batchPersisted) {
        // no-op.
    }

    @Override
    public List<DownloadsBatchPersisted> loadBatches() {
        Set<DownloadsBatchPersisted> batches = filesByBatches.keySet();
        return new ArrayList<>(batches);
    }

    @Override
    public void persistFile(DownloadsFilePersisted filePersisted) {
        // no-op.
    }

    @Override
    public List<DownloadsFilePersisted> loadAllFiles() {
        List<DownloadsFilePersisted> allFiles = new ArrayList<>();
        for (Map.Entry<DownloadsBatchPersisted, List<DownloadsFilePersisted>> entry : filesByBatches.entrySet()) {
            allFiles.addAll(entry.getValue());
        }
        return allFiles;
    }

    @Override
    public List<DownloadsFilePersisted> loadFiles(final DownloadBatchId batchId) {
        for (Map.Entry<DownloadsBatchPersisted, List<DownloadsFilePersisted>> batchWithFiles : filesByBatches.entrySet()) {
            if (batchId.rawId().equals(batchWithFiles.getKey().downloadBatchId().rawId())) {
                return batchWithFiles.getValue();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean delete(DownloadBatchId downloadBatchId) {
        return true;
    }

    @Override
    public boolean update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
        return true;
    }

    @Override
    public boolean update(DownloadBatchId downloadBatchId, boolean notificationSeen) {
        return true;
    }
}
