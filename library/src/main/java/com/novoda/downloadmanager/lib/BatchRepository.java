package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.novoda.downloadmanager.notifications.NotificationVisibility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class BatchRepository {

    private final ContentResolver resolver;
    private final DownloadsUriProvider downloadsUriProvider;
    private final BatchStatusService batchStatusService;
    private final BatchStartingService batchStartingService;
    private final BatchDeletionService batchDeletionService;

    BatchRepository(ContentResolver resolver, DownloadDeleter downloadDeleter, DownloadsUriProvider downloadsUriProvider, SystemFacade systemFacade) {
        this.resolver = resolver;
        this.downloadsUriProvider = downloadsUriProvider;
        this.batchStatusService = new BatchStatusService(resolver, downloadsUriProvider, systemFacade);
        this.batchStartingService = new BatchStartingService(resolver, downloadsUriProvider);
        this.batchDeletionService = new BatchDeletionService(downloadDeleter, resolver, downloadsUriProvider);
    }

    void updateBatchStatus(long batchId, int status) {
        batchStatusService.updateBatchStatus(batchId, status);
    }

    int getBatchStatus(long batchId) {
        return batchStatusService.getBatchStatus(batchId);
    }

    int calculateBatchStatus(long batchId) {
        return batchStatusService.calculateBatchStatusFromDownloads(batchId);
    }

    public void setBatchItemsCancelled(long batchId) {
        batchStatusService.setBatchItemsCancelled(batchId);
    }

    public void cancelBatch(long batchId) {
        batchStatusService.cancelBatch(batchId);
    }

    public void setBatchItemsFailed(long batchId, long downloadId) {
        batchStatusService.setBatchItemsFailed(batchId, downloadId);
    }

    /**
     * @return Number of rows updated
     */
    int updateBatchesToPendingStatus(@NonNull List<String> batchIdsToBeUnlocked) {
        return batchStatusService.updateBatchToPendingStatus(batchIdsToBeUnlocked);
    }

    boolean isBatchStartingForTheFirstTime(long batchId) {
        return batchStartingService.isBatchStartingForTheFirstTime(batchId);
    }

    public void markBatchAsStarted(long batchId) {
        batchStartingService.markMatchAsStarted(batchId);
    }

    public DownloadBatch retrieveBatchFor(FileDownloadInfo download) {
        Collection<FileDownloadInfo> downloads = Collections.singletonList(download);
        List<DownloadBatch> batches = retrieveBatchesFor(downloads);

        for (DownloadBatch batch : batches) {
            if (batch.getBatchId() == download.getBatchId()) {
                return batch;
            }
        }

        return DownloadBatch.DELETED;
    }

    public List<DownloadBatch> retrieveBatchesFor(Collection<FileDownloadInfo> downloads) {
        Cursor batchesCursor = resolver.query(this.downloadsUriProvider.getBatchesUri(), null, null, null, null);
        List<DownloadBatch> batches = new ArrayList<>(batchesCursor.getCount());
        try {
            int idColumn = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches._ID);
            int titleIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_TITLE);
            int descriptionIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_DESCRIPTION);
            int bigPictureUrlIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_BIG_PICTURE);
            int statusIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_STATUS);
            int visibilityIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_VISIBILITY);
            int extraDataIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_EXTRA_DATA);
            int totalBatchSizeIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.BatchesWithSizes.COLUMN_TOTAL_BYTES);
            int currentBatchSizeIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.BatchesWithSizes.COLUMN_CURRENT_BYTES);

            while (batchesCursor.moveToNext()) {
                long id = batchesCursor.getLong(idColumn);
                String title = batchesCursor.getString(titleIndex);
                String description = batchesCursor.getString(descriptionIndex);
                String bigPictureUrl = batchesCursor.getString(bigPictureUrlIndex);
                int status = batchesCursor.getInt(statusIndex);
                @NotificationVisibility.Value int visibility = batchesCursor.getInt(visibilityIndex);
                String extraData = batchesCursor.getString(extraDataIndex);
                long totalSizeBytes = batchesCursor.getLong(totalBatchSizeIndex);
                long currentSizeBytes = batchesCursor.getLong(currentBatchSizeIndex);
                BatchInfo batchInfo = new BatchInfo(title, description, bigPictureUrl, visibility, extraData);

                List<FileDownloadInfo> batchDownloads = new ArrayList<>(1);
                for (FileDownloadInfo fileDownloadInfo : downloads) {
                    if (fileDownloadInfo.getBatchId() == id) {
                        batchDownloads.add(fileDownloadInfo);
                    }
                }
                batches.add(new DownloadBatch(id, batchInfo, batchDownloads, status, totalSizeBytes, currentSizeBytes));
            }
        } finally {
            batchesCursor.close();
        }

        return batches;
    }

    public void deleteMarkedBatchesFor(Collection<FileDownloadInfo> downloads) {
        batchDeletionService.deleteMarkedBatchesFor(downloads);
    }

    public Cursor retrieveFor(BatchQuery query) {
        return resolver.query(downloadsUriProvider.getBatchesUri(), null, query.getSelection(), query.getSelectionArguments(), query.getSortOrder());
    }
}
