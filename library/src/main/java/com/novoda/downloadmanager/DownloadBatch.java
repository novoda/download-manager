package com.novoda.downloadmanager;

import java.util.List;
import java.util.Map;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.*;

class DownloadBatch {

    private static final int ZERO_BYTES = 0;

    private final DownloadBatchId downloadBatchId;
    private final DownloadBatchTitle downloadBatchTitle;
    private final Map<DownloadFileId, Long> fileBytesDownloadedMap;
    private final InternalDownloadBatchStatus downloadBatchStatus;
    private final List<DownloadFile> downloadFiles;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final CallbackThrottle callbackThrottle;

    private long totalBatchSizeBytes;
    private DownloadBatchCallback callback;

    DownloadBatch(DownloadBatchTitle downloadBatchTitle,
                  DownloadBatchId downloadBatchId,
                  List<DownloadFile> downloadFiles,
                  Map<DownloadFileId, Long> fileBytesDownloadedMap,
                  InternalDownloadBatchStatus internalDownloadBatchStatus,
                  DownloadsBatchPersistence downloadsBatchPersistence,
                  CallbackThrottle callbackThrottle) {
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadBatchId = downloadBatchId;
        this.downloadFiles = downloadFiles;
        this.fileBytesDownloadedMap = fileBytesDownloadedMap;
        this.downloadBatchStatus = internalDownloadBatchStatus;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.callbackThrottle = callbackThrottle;
    }

    void setCallback(DownloadBatchCallback callback) {
        this.callback = callback;
        callbackThrottle.setCallback(callback);
    }

    void download() {
        if (downloadBatchStatus.status() == PAUSED) {
            return;
        }

        if (downloadBatchStatus.status() == DELETION) {
            return;
        }

        downloadBatchStatus.markAsDownloading(downloadsBatchPersistence);
        notifyCallback(downloadBatchStatus);

        totalBatchSizeBytes = getTotalSize(downloadFiles);

        if (totalBatchSizeBytes <= ZERO_BYTES) {
            DownloadError downloadError = new DownloadError();
            downloadError.setError(DownloadError.Error.NETWORK_ERROR_CANNOT_DOWNLOAD_FILE);
            downloadBatchStatus.markAsError(downloadError);
            notifyCallback(downloadBatchStatus);
            return;
        }

        DownloadFile.Callback fileDownloadCallback = new DownloadFile.Callback() {

            @Override
            public void onUpdate(DownloadFileStatus downloadFileStatus) {
                fileBytesDownloadedMap.put(downloadFileStatus.getDownloadFileId(), downloadFileStatus.bytesDownloaded());
                long currentBytesDownloaded = getBytesDownloadedFrom(fileBytesDownloadedMap);
                downloadBatchStatus.update(currentBytesDownloaded, totalBatchSizeBytes);

                if (downloadFileStatus.isMarkedAsError()) {
                    downloadBatchStatus.markAsError(downloadFileStatus.getError());
                }

                callbackThrottle.update(downloadBatchStatus);
            }
        };

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.download(fileDownloadCallback);
            if (batchCannotContinue()) {
                break;
            }
        }

        if (networkError()) {
            DownloadsNetworkRecoveryCreator.getInstance().scheduleRecovery();
        }

        if (downloadBatchStatus.status() == DOWNLOADED) {
            downloadBatchStatus.markAsDownloaded(downloadsBatchPersistence);
        }

        callbackThrottle.stopUpdates();
    }

    private boolean networkError() {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status == ERROR) {
            DownloadError.Error downloadErrorType = downloadBatchStatus.getDownloadErrorType();
            if (downloadErrorType == DownloadError.Error.NETWORK_ERROR_CANNOT_DOWNLOAD_FILE) {
                return true;
            }
        }
        return false;
    }

    private boolean batchCannotContinue() {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        return status == ERROR || status == DELETION || status == PAUSED;
    }

    private long getBytesDownloadedFrom(Map<DownloadFileId, Long> fileBytesDownloadedMap) {
        long bytesDownloaded = 0;
        for (Map.Entry<DownloadFileId, Long> entry : fileBytesDownloadedMap.entrySet()) {
            bytesDownloaded += entry.getValue();
        }
        return bytesDownloaded;
    }

    private void notifyCallback(InternalDownloadBatchStatus downloadBatchStatus) {
        if (callback != null) {
            callback.onUpdate(downloadBatchStatus);
        }
    }

    private long getTotalSize(List<DownloadFile> downloadFiles) {
        if (totalBatchSizeBytes == 0) {
            for (DownloadFile downloadFile : downloadFiles) {
                totalBatchSizeBytes += downloadFile.getTotalSize();
            }
        }

        return totalBatchSizeBytes;
    }

    void pause() {
        if (downloadBatchStatus.status() == PAUSED) {
            return;
        }
        downloadBatchStatus.markAsPaused(downloadsBatchPersistence);
        notifyCallback(downloadBatchStatus);

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.pause();
        }
    }

    void resume() {
        if (downloadBatchStatus.status() == QUEUED || downloadBatchStatus.status() == DOWNLOADING) {
            return;
        }
        downloadBatchStatus.markAsQueued(downloadsBatchPersistence);
        notifyCallback(downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.resume();
        }
    }

    void delete() {
        downloadsBatchPersistence.deleteAsync(downloadBatchId);
        downloadBatchStatus.markForDeletion();
        notifyCallback(downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.delete();
        }
    }

    DownloadBatchId getId() {
        return downloadBatchId;
    }

    InternalDownloadBatchStatus status() {
        return downloadBatchStatus;
    }

    void persist() {
        downloadsBatchPersistence.persistAsync(downloadBatchTitle, downloadBatchId, downloadBatchStatus.status(), downloadFiles);
    }
}
