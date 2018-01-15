package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETION;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADING;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.ERROR;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.PAUSED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.QUEUED;

// This model knows how to interact with low level components.
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
class DownloadBatch {

    private static final int ZERO_BYTES = 0;

    private final Map<DownloadFileId, Long> fileBytesDownloadedMap;
    private final InternalDownloadBatchStatus downloadBatchStatus;
    private final List<DownloadFile> downloadFiles;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final CallbackThrottle callbackThrottle;

    private long totalBatchSizeBytes;
    private DownloadBatchCallback callback;

    DownloadBatch(InternalDownloadBatchStatus internalDownloadBatchStatus,
                  List<DownloadFile> downloadFiles,
                  Map<DownloadFileId, Long> fileBytesDownloadedMap,
                  DownloadsBatchPersistence downloadsBatchPersistence,
                  CallbackThrottle callbackThrottle) {
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

        if (downloadBatchStatus.status() == DOWNLOADED) {
            return;
        }

        downloadBatchStatus.markAsDownloading(downloadsBatchPersistence);
        notifyCallback(downloadBatchStatus);

        totalBatchSizeBytes = getTotalSize(downloadFiles);

        if (totalBatchSizeBytes <= ZERO_BYTES) {
            DownloadError downloadError = new DownloadError(DownloadError.Error.NETWORK_ERROR_CANNOT_DOWNLOAD_FILE);
            downloadBatchStatus.markAsError(downloadError, downloadsBatchPersistence);
            notifyCallback(downloadBatchStatus);
            return;
        }

        DownloadFile.Callback fileDownloadCallback = downloadFileStatus -> {
            fileBytesDownloadedMap.put(downloadFileStatus.downloadFileId(), downloadFileStatus.bytesDownloaded());
            long currentBytesDownloaded = getBytesDownloadedFrom(fileBytesDownloadedMap);
            downloadBatchStatus.update(currentBytesDownloaded, totalBatchSizeBytes);

            if (downloadFileStatus.isMarkedAsError()) {
                downloadBatchStatus.markAsError(downloadFileStatus.error(), downloadsBatchPersistence);
            }

            callbackThrottle.update(downloadBatchStatus);
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
        downloadsBatchPersistence.deleteAsync(downloadBatchStatus.getDownloadBatchId());
        downloadBatchStatus.markForDeletion();
        notifyCallback(downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.delete();
        }
    }

    DownloadBatchId getId() {
        return downloadBatchStatus.getDownloadBatchId();
    }

    InternalDownloadBatchStatus status() {
        return downloadBatchStatus;
    }

    @Nullable
    DownloadFileStatus downloadFileStatusWith(DownloadFileId downloadFileId) {
        for (DownloadFile downloadFile : downloadFiles) {
            if (downloadFile.matches(downloadFileId)) {
                return downloadFile.fileStatus();
            }
        }
        return null;
    }

    void persist() {
        downloadsBatchPersistence.persistAsync(
                downloadBatchStatus.getDownloadBatchTitle(),
                downloadBatchStatus.getDownloadBatchId(),
                downloadBatchStatus.status(),
                downloadFiles,
                downloadBatchStatus.downloadedDateTimeInMillis()
        );
    }
}
