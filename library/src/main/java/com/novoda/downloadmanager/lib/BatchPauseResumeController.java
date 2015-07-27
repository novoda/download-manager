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

    public void pauseBatch(long batchId) throws BatchPauseException {
        int batchStatus = batchRepository.getBatchStatus(batchId);
        if (DownloadStatus.isRunning(batchStatus)) {
            downloadsRepository.pauseDownloadWithBatchId(batchId);
        } else {
            throw new BatchPauseException("Batch " + batchId + " cannot be paused as is not currently running");
        }
    }

    public void resumeBatch(long batchId) throws BatchResumeException {
        int batchStatus = batchRepository.getBatchStatus(batchId);
        if (DownloadStatus.isPausedByApp(batchStatus)) {
            downloadsRepository.resumeDownloadWithBatchId(batchId);
            batchRepository.updateBatchStatus(batchId, DownloadStatus.PENDING);
            notifyBatchesHaveChanged();
        } else {
            throw new BatchResumeException("Batch " + batchId + " cannot be resumed as is not currently paused");
        }
    }

    private void notifyBatchesHaveChanged() {
        contentResolver.notifyChange(downloadsUriProvider.getBatchesUri(), null);
        contentResolver.notifyChange(downloadsUriProvider.getBatchesWithoutProgressUri(), null);
    }

    public static class BatchPauseException extends Exception {
        public BatchPauseException(String message) {
            super(message);
        }
    }

    public static class BatchResumeException extends Exception {
        public BatchResumeException(String message) {
            super(message);
        }
    }
}
