package com.novoda.downloadmanager;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;
import java.util.Map;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETING;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADING;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.ERROR;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.PAUSED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.QUEUED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.WAITING_FOR_NETWORK;

// This model knows how to interact with low level components.
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
class DownloadBatch {

    private static final int ZERO_BYTES = 0;

    private final Map<DownloadFileId, Long> fileBytesDownloadedMap;
    private final InternalDownloadBatchStatus downloadBatchStatus;
    private final List<DownloadFile> downloadFiles;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final CallbackThrottle callbackThrottle;
    private final ConnectionChecker connectionChecker;

    private long totalBatchSizeBytes;
    private DownloadBatchStatusCallback callback;

    DownloadBatch(InternalDownloadBatchStatus internalDownloadBatchStatus,
                  List<DownloadFile> downloadFiles,
                  Map<DownloadFileId, Long> fileBytesDownloadedMap,
                  DownloadsBatchPersistence downloadsBatchPersistence,
                  CallbackThrottle callbackThrottle,
                  ConnectionChecker connectionChecker) {
        this.downloadFiles = downloadFiles;
        this.fileBytesDownloadedMap = fileBytesDownloadedMap;
        this.downloadBatchStatus = internalDownloadBatchStatus;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.callbackThrottle = callbackThrottle;
        this.connectionChecker = connectionChecker;
    }

    void setCallback(DownloadBatchStatusCallback callback) {
        this.callback = callback;
        callbackThrottle.setCallback(callback);
    }

    void download() {
        String rawBatchId = downloadBatchStatus.getDownloadBatchId().rawId();
        Logger.v("start download " + rawBatchId + ", status: " + downloadBatchStatus.status());

        if (shouldAbortStartingBatch(connectionChecker, callback, downloadBatchStatus, downloadsBatchPersistence)) {
            Logger.v("abort starting download " + rawBatchId);
            return;
        }

        markAsDownloadingIfNeeded(downloadBatchStatus, downloadsBatchPersistence, callback);

        if (totalBatchSizeBytes == 0) {
            totalBatchSizeBytes = getTotalSize(downloadFiles, downloadBatchStatus);
        }

        if (shouldAbortAfterGettingTotalBatchSize(downloadBatchStatus, downloadsBatchPersistence, callback, totalBatchSizeBytes)) {
            Logger.v("abort after getting total batch size download " + rawBatchId);
            return;
        }

        for (DownloadFile downloadFile : downloadFiles) {
            if (batchCannotContinue(downloadBatchStatus, connectionChecker, downloadsBatchPersistence, callback)) {
                break;
            }
            downloadFile.download(fileDownloadCallback);
        }

        if (networkError(downloadBatchStatus)) {
            processNetworkError(downloadBatchStatus, callback, downloadsBatchPersistence);
        }

        deleteBatchIfNeeded(downloadBatchStatus, downloadsBatchPersistence, callback);
        notifyCallback(callback, downloadBatchStatus);
        callbackThrottle.stopUpdates();
        Logger.v("end download " + rawBatchId);
    }

    private static boolean shouldAbortStartingBatch(ConnectionChecker connectionChecker,
                                                    DownloadBatchStatusCallback callback,
                                                    InternalDownloadBatchStatus downloadBatchStatus,
                                                    DownloadsBatchPersistence downloadsBatchPersistence) {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();

        if (status == DELETED) {
            return true;
        }

        if (status == DELETING) {
            deleteBatchIfNeeded(downloadBatchStatus, downloadsBatchPersistence, callback);
            notifyCallback(callback, downloadBatchStatus);
            return true;
        }

        if (status == PAUSED) {
            notifyCallback(callback, downloadBatchStatus);
            return true;
        }

        if (connectionNotAllowedForDownload(connectionChecker, status)) {
            processNetworkError(downloadBatchStatus, callback, downloadsBatchPersistence);
            notifyCallback(callback, downloadBatchStatus);
            return true;
        }

        return false;
    }

    private static void deleteBatchIfNeeded(InternalDownloadBatchStatus downloadBatchStatus,
                                            DownloadsBatchPersistence downloadsBatchPersistence,
                                            DownloadBatchStatusCallback callback) {
        if (downloadBatchStatus.status() == DELETING && downloadsBatchPersistence.deleteSync(downloadBatchStatus)) {
            downloadBatchStatus.markAsDeleted();
            notifyCallback(callback, downloadBatchStatus);
        }
    }

    private static void notifyCallback(DownloadBatchStatusCallback callback, InternalDownloadBatchStatus downloadBatchStatus) {
        if (callback != null) {
            callback.onUpdate(downloadBatchStatus.copy());
        }
    }

    private static boolean connectionNotAllowedForDownload(ConnectionChecker connectionChecker, DownloadBatchStatus.Status status) {
        return !connectionChecker.isAllowedToDownload() && status != DOWNLOADED;
    }

    private static void processNetworkError(InternalDownloadBatchStatus downloadBatchStatus,
                                            DownloadBatchStatusCallback callback,
                                            DownloadsBatchPersistence downloadsBatchPersistence) {
        if (downloadBatchStatus.status() == DELETING) {
            return;
        }
        downloadBatchStatus.markAsWaitingForNetwork(downloadsBatchPersistence);
        notifyCallback(callback, downloadBatchStatus);
        DownloadsNetworkRecoveryCreator.getInstance().scheduleRecovery();
    }

    private static void markAsDownloadingIfNeeded(InternalDownloadBatchStatus downloadBatchStatus,
                                                  DownloadsBatchPersistence downloadsBatchPersistence,
                                                  DownloadBatchStatusCallback callback) {
        if (downloadBatchStatus.status() != DOWNLOADED) {
            downloadBatchStatus.markAsDownloading(downloadsBatchPersistence);
            notifyCallback(callback, downloadBatchStatus);
        }
    }

    private static long getTotalSize(List<DownloadFile> downloadFiles, InternalDownloadBatchStatus downloadBatchStatus) {
        long totalBatchSize = 0;
        for (DownloadFile downloadFile : downloadFiles) {
            DownloadBatchStatus.Status status = downloadBatchStatus.status();
            if (status == DELETING || status == DELETED || status == PAUSED) {
                return 0;
            }

            Logger.v("batch id: " + downloadBatchStatus.getDownloadBatchId().rawId() + ", status: " + status + ", file: " + downloadFile.id().rawId());

            long totalFileSize = downloadFile.getTotalSize();
            if (totalFileSize == 0) {
                return 0;
            }

            totalBatchSize += totalFileSize;
        }
        return totalBatchSize;
    }

    private static boolean shouldAbortAfterGettingTotalBatchSize(InternalDownloadBatchStatus downloadBatchStatus,
                                                                 DownloadsBatchPersistence downloadsBatchPersistence,
                                                                 DownloadBatchStatusCallback callback,
                                                                 long totalBatchSizeBytes) {
        if (downloadBatchStatus.status() == PAUSED) {
            notifyCallback(callback, downloadBatchStatus);
            return true;
        }

        if (downloadBatchStatus.status() == DELETING) {
            deleteBatchIfNeeded(downloadBatchStatus, downloadsBatchPersistence, callback);
            notifyCallback(callback, downloadBatchStatus);
            return true;
        }

        if (totalBatchSizeBytes <= ZERO_BYTES) {
            processNetworkError(downloadBatchStatus, callback, downloadsBatchPersistence);
            notifyCallback(callback, downloadBatchStatus);
            return true;
        }

        return false;
    }

    private static boolean batchCannotContinue(InternalDownloadBatchStatus downloadBatchStatus,
                                               ConnectionChecker connectionChecker,
                                               DownloadsBatchPersistence downloadsBatchPersistence,
                                               DownloadBatchStatusCallback callback) {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();

        if (connectionNotAllowedForDownload(connectionChecker, status)) {
            downloadBatchStatus.markAsWaitingForNetwork(downloadsBatchPersistence);
            notifyCallback(callback, downloadBatchStatus);
            return true;
        } else {
            return status == ERROR || status == DELETING || status == DELETED || status == PAUSED || status == WAITING_FOR_NETWORK;
        }
    }

    private final DownloadFile.Callback fileDownloadCallback = new DownloadFile.Callback() {
        @Override
        public void onUpdate(InternalDownloadFileStatus downloadFileStatus) {
            fileBytesDownloadedMap.put(downloadFileStatus.downloadFileId(), downloadFileStatus.bytesDownloaded());
            long currentBytesDownloaded = getBytesDownloadedFrom(fileBytesDownloadedMap);
            downloadBatchStatus.update(currentBytesDownloaded, totalBatchSizeBytes);

            if (currentBytesDownloaded == totalBatchSizeBytes && totalBatchSizeBytes != ZERO_BYTES) {
                downloadBatchStatus.markAsDownloaded(downloadsBatchPersistence);
            }

            if (downloadFileStatus.isMarkedAsError()) {
                downloadBatchStatus.markAsError(downloadFileStatus.error(), downloadsBatchPersistence);
            }

            if (downloadFileStatus.isMarkedAsWaitingForNetwork()) {
                downloadBatchStatus.markAsWaitingForNetwork(downloadsBatchPersistence);
            }

            callbackThrottle.update(downloadBatchStatus);
        }
    };

    private static long getBytesDownloadedFrom(Map<DownloadFileId, Long> fileBytesDownloadedMap) {
        long bytesDownloaded = 0;
        for (Map.Entry<DownloadFileId, Long> entry : fileBytesDownloadedMap.entrySet()) {
            bytesDownloaded += entry.getValue();
        }
        return bytesDownloaded;
    }

    private static boolean networkError(InternalDownloadBatchStatus downloadBatchStatus) {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status == WAITING_FOR_NETWORK) {
            return true;
        } else if (status == ERROR) {
            DownloadError.Error downloadErrorType = downloadBatchStatus.getDownloadErrorType();
            if (downloadErrorType == DownloadError.Error.NETWORK_ERROR_CANNOT_DOWNLOAD_FILE) {
                return true;
            }
        }
        return false;
    }

    void pause() {
        Logger.v("pause batch " + downloadBatchStatus.getDownloadBatchId().rawId() + ", status: " + downloadBatchStatus.status());
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status == PAUSED || status == DOWNLOADED) {
            return;
        }
        downloadBatchStatus.markAsPaused(downloadsBatchPersistence);
        notifyCallback(callback, downloadBatchStatus);

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.pause();
        }
    }

    void waitForNetwork() {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status != DOWNLOADING) {
            return;
        }

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.waitForNetwork();
        }
    }

    void resume() {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status == QUEUED || status == DOWNLOADING || status == DOWNLOADED) {
            return;
        }
        downloadBatchStatus.markAsQueued(downloadsBatchPersistence);
        notifyCallback(callback, downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.resume();
        }
    }

    void delete() {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status == DELETING || status == DELETED) {
            return;
        }

        Logger.v("delete batch " + downloadBatchStatus.getDownloadBatchId().rawId() + ", mark as deleting from " + downloadBatchStatus.status());
        downloadBatchStatus.markAsDeleting();
        notifyCallback(callback, downloadBatchStatus);

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.delete();
        }

        if (status == PAUSED || status == DOWNLOADED) {
            Logger.v("delete paused or downloaded batch " + downloadBatchStatus.getDownloadBatchId().rawId());
            downloadsBatchPersistence.deleteAsync(downloadBatchStatus, downloadBatchId -> {
                Logger.v("delete paused or downloaded mark as deleted: " + downloadBatchId.rawId());
                downloadBatchStatus.markAsDeleted();
                notifyCallback(callback, downloadBatchStatus);
            });
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

    void persistAsync() {
        downloadsBatchPersistence.persistAsync(
                downloadBatchStatus.getDownloadBatchTitle(),
                downloadBatchStatus.getDownloadBatchId(),
                downloadBatchStatus.status(),
                downloadFiles,
                downloadBatchStatus.downloadedDateTimeInMillis(),
                downloadBatchStatus.notificationSeen()
        );
    }

    @WorkerThread
    void persist() {
        downloadsBatchPersistence.persist(
                downloadBatchStatus.getDownloadBatchTitle(),
                downloadBatchStatus.getDownloadBatchId(),
                downloadBatchStatus.status(),
                downloadFiles,
                downloadBatchStatus.downloadedDateTimeInMillis(),
                downloadBatchStatus.notificationSeen()
        );
    }
}
