package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;

public class BatchPauseResumeController {

    private final BatchRepository batchRepository;
    private final DownloadsRepository downloadsRepository;
    private final ContentResolver contentResolver;
    private final DownloadsUriProvider downloadsUriProvider;

    BatchPauseResumeController(ContentResolver contentResolver,
                               DownloadsUriProvider downloadsUriProvider,
                               BatchRepository batchRepository,
                               DownloadsRepository downloadsRepository) {
        this.contentResolver = contentResolver;
        this.batchRepository = batchRepository;
        this.downloadsRepository = downloadsRepository;
        this.downloadsUriProvider = downloadsUriProvider;
    }

    /**
     * Returns true if the batch was paused, false otherwise
     */
    public boolean pauseBatch(long batchId) {
        int batchStatus = batchRepository.getBatchStatus(batchId);
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
        int batchStatus = batchRepository.getBatchStatus(batchId);
        if (DownloadStatus.isPausedByApp(batchStatus)) {
            downloadsRepository.resumeDownloadWithBatchId(batchId);
            batchRepository.updateBatchStatus(batchId, DownloadStatus.PENDING);
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

    public void unlockStaleDownloads() {
        String batchToBeUnlocked = downloadsRepository.getCurrentDownloadingBatchId();
        if (batchToBeUnlocked == null) {
            batchToBeUnlocked = downloadsRepository.getCurrentSubmittedBatchId();
        }
        if (batchToBeUnlocked == null) {
            return;
        }
        downloadsRepository.updateRunningOrSubmittedDownloadsToPending();
        batchRepository.updateBatchToPendingStatus(batchToBeUnlocked);
    }
}
