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

    }

    @Override
    public void endTransaction() {

    }

    @Override
    public void transactionSuccess() {

    }

    @Override
    public void persistBatch(DownloadsBatchPersisted batchPersisted) {

    }

    @Override
    public List<DownloadsBatchPersisted> loadBatches() {
        Set<DownloadsBatchPersisted> batches = filesByBatches.keySet();
        return new ArrayList<>(batches);
    }

    @Override
    public void persistFile(DownloadsFilePersisted filePersisted) {

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
    public void delete(DownloadBatchId downloadBatchId) {

    }

    @Override
    public void update(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {

    }
}
