package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.List;

class BatchRepository {

    private final BatchStatusRepository batchStatusRepository;
    private final BatchStartingRepository batchStartingRepository;
    private final BatchDeletionRepository batchDeletionRepository;
    private final BatchRetrievalRepository batchRetrievalRepository;
    
    static BatchRepository from(ContentResolver resolver,
                            DownloadDeleter downloadDeleter,
                            DownloadsUriProvider downloadsUriProvider,
                            SystemFacade systemFacade) {
        BatchStatusRepository batchStatusRepository = new BatchStatusRepository(resolver, downloadsUriProvider, systemFacade);
        BatchStartingRepository batchStartingRepository = new BatchStartingRepository(resolver, downloadsUriProvider);
        BatchDeletionRepository batchDeletionRepository = new BatchDeletionRepository(downloadDeleter, resolver, downloadsUriProvider);
        BatchRetrievalRepository batchRetrievalRepository = new BatchRetrievalRepository(resolver, downloadsUriProvider);

        return new BatchRepository(batchStatusRepository, batchStartingRepository, batchDeletionRepository, batchRetrievalRepository);

    }

    BatchRepository(BatchStatusRepository batchStatusRepository,
                    BatchStartingRepository batchStartingRepository,
                    BatchDeletionRepository batchDeletionRepository,
                    BatchRetrievalRepository batchRetrievalRepository) {
        this.batchStatusRepository = batchStatusRepository;
        this.batchStartingRepository = batchStartingRepository;
        this.batchDeletionRepository = batchDeletionRepository;
        this.batchRetrievalRepository = batchRetrievalRepository;
    }

    void updateBatchStatus(long batchId, int status) {
        batchStatusRepository.updateBatchStatus(batchId, status);
    }

    int getBatchStatus(long batchId) {
        return batchStatusRepository.getBatchStatus(batchId);
    }

    int calculateBatchStatus(long batchId) {
        return batchStatusRepository.calculateBatchStatusFromDownloads(batchId);
    }

    public void setBatchItemsCancelled(long batchId) {
        batchStatusRepository.setBatchItemsCancelled(batchId);
    }

    public void cancelBatch(long batchId) {
        batchStatusRepository.cancelBatch(batchId);
    }

    public void setBatchItemsFailed(long batchId, long downloadId) {
        batchStatusRepository.setBatchItemsFailed(batchId, downloadId);
    }

    /**
     * @return Number of rows updated
     */
    int updateBatchesToPendingStatus(@NonNull List<String> batchIdsToBeUnlocked) {
        return batchStatusRepository.updateBatchToPendingStatus(batchIdsToBeUnlocked);
    }

    boolean isBatchStartingForTheFirstTime(long batchId) {
        return batchStartingRepository.isBatchStartingForTheFirstTime(batchId);
    }

    public void markBatchAsStarted(long batchId) {
        batchStartingRepository.markBatchAsStarted(batchId);
    }

    public DownloadBatch retrieveBatchFor(FileDownloadInfo download) {
        return batchRetrievalRepository.retrieveBatchFor(download);
    }

    public List<DownloadBatch> retrieveBatchesFor(Collection<FileDownloadInfo> downloads) {
        return batchRetrievalRepository.retrieveBatchesFor(downloads);
    }

    public Cursor retrieveFor(BatchQuery query) {
        return batchRetrievalRepository.retrieveFor(query);
    }

    public void deleteMarkedBatchesFor(Collection<FileDownloadInfo> downloads) {
        batchDeletionRepository.deleteMarkedBatchesFor(downloads);
    }
}
