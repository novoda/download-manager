package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;

public class BatchPauseResumeController {

    private final BatchFacade batchFacade;
    private final DownloadsRepository downloadsRepository;
    private final ContentResolver contentResolver;
    private final DownloadsUriProvider downloadsUriProvider;

    BatchPauseResumeController(ContentResolver contentResolver,
                               DownloadsUriProvider downloadsUriProvider,
                               BatchFacade batchFacade,
                               DownloadsRepository downloadsRepository) {
        this.contentResolver = contentResolver;
        this.batchFacade = batchFacade;
        this.downloadsRepository = downloadsRepository;
        this.downloadsUriProvider = downloadsUriProvider;
    }

    /**
     * Returns true if the batch was paused, false otherwise
     */
    public boolean pauseBatch(long batchId) {
        int batchStatus = batchFacade.getBatchStatus(batchId);
        if (DownloadStatus.isRunning(batchStatus)) {
            downloadsRepository.pauseDownloadWithBatchId(batchId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the batch was resumed, false otherwise
     */
    public boolean resumeBatch(long batchId) {
        int batchStatus = batchFacade.getBatchStatus(batchId);
        if (DownloadStatus.isPausedByApp(batchStatus)) {
            downloadsRepository.resumeDownloadWithBatchId(batchId);
            batchFacade.updateBatchStatus(batchId, DownloadStatus.PENDING);
            notifyBatchesHaveChanged();
            return true;
        } else {
            return false;
        }
    }

    private void notifyBatchesHaveChanged() {
        contentResolver.notifyChange(downloadsUriProvider.getBatchesUri(), null);
        contentResolver.notifyChange(downloadsUriProvider.getBatchesWithoutProgressUri(), null);
    }
}
