package com.novoda.downloadmanager;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;
import java.util.Map;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETING;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADING;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.PAUSED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.ERROR;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.WAITING_FOR_NETWORK;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.QUEUED;

// This model knows how to interact with low level components.
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
class DownloadBatch {

    private static final int ZERO_BYTES = 0;
    private static final String STATUS = "status";

    private final Map<DownloadFileId, Long> fileBytesDownloadedMap;
    private final InternalDownloadBatchStatus downloadBatchStatus;
    private final List<DownloadFile> downloadFiles;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final CallbackThrottle callbackThrottle;
    private final ConnectionChecker connectionChecker;

    private long totalBatchSizeBytes;

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
        callbackThrottle.setCallback(callback);
    }

    void download() {
        String rawBatchId = downloadBatchStatus.getDownloadBatchId().rawId();
        Logger.v("start sync download " + rawBatchId + ", " + STATUS + " " + downloadBatchStatus.status());

        if (shouldAbortStartingBatch(connectionChecker, callbackThrottle, downloadBatchStatus, downloadsBatchPersistence)) {
            Logger.v("abort starting download " + rawBatchId + ", " + STATUS + " " + downloadBatchStatus.status());
            return;
        }

        markAsDownloadingIfNeeded(downloadBatchStatus, downloadsBatchPersistence, callbackThrottle);

        updateTotalSize();

        Logger.v("batch " + downloadBatchStatus.getDownloadBatchId().rawId()
                         + " " + STATUS + " " + downloadBatchStatus.status()
                         + " totalBatchSize " + totalBatchSizeBytes);

        if (shouldAbortAfterGettingTotalBatchSize(downloadBatchStatus, downloadsBatchPersistence, callbackThrottle, totalBatchSizeBytes)) {
            Logger.v("abort after getting total batch size download " + rawBatchId + ", " + STATUS + " " + downloadBatchStatus.status());
            return;
        }

        for (DownloadFile downloadFile : downloadFiles) {
            if (batchCannotContinue(downloadBatchStatus, connectionChecker, downloadsBatchPersistence, callbackThrottle)) {
                break;
            }
            downloadFile.download(fileDownloadCallback);
        }

        if (networkError(downloadBatchStatus)) {
            processNetworkError(downloadBatchStatus, callbackThrottle, downloadsBatchPersistence);
        }

        deleteBatchIfNeeded(downloadBatchStatus, downloadsBatchPersistence, callbackThrottle);
        Logger.v("end sync download " + rawBatchId);
    }

    private static boolean shouldAbortStartingBatch(ConnectionChecker connectionChecker,
                                                    CallbackThrottle callbackThrottle,
                                                    InternalDownloadBatchStatus downloadBatchStatus,
                                                    DownloadsBatchPersistence downloadsBatchPersistence) {
        // WARNING: do not extract downloadBatchStatus.status() as a local variable, this will
        // invalidate the checks when a batch is deleted from the main thread, as this code
        // runs in a thread and its status can change at any point.
        // deleteBatchIfNeeded() is an expensive task that will take few milliseconds, after that
        // time the status might be different
        if (downloadBatchStatus.status() == DELETED) {
            return true;
        }

        if (downloadBatchStatus.status() == DELETING) {
            deleteBatchIfNeeded(downloadBatchStatus, downloadsBatchPersistence, callbackThrottle);
            notifyCallback(callbackThrottle, downloadBatchStatus);
            return true;
        }

        if (downloadBatchStatus.status() == PAUSED) {
            notifyCallback(callbackThrottle, downloadBatchStatus);
            return true;
        }

        if (connectionNotAllowedForDownload(connectionChecker, downloadBatchStatus.status())) {
            processNetworkError(downloadBatchStatus, callbackThrottle, downloadsBatchPersistence);
            notifyCallback(callbackThrottle, downloadBatchStatus);
            return true;
        }

        if (downloadBatchStatus.status() == DOWNLOADED) {
            notifyCallback(callbackThrottle, downloadBatchStatus);
            return true;
        }

        return false;
    }

    private static void deleteBatchIfNeeded(InternalDownloadBatchStatus downloadBatchStatus,
                                            DownloadsBatchPersistence downloadsBatchPersistence,
                                            CallbackThrottle callbackThrottle) {
        if (downloadBatchStatus.status() == DELETING) {
            Logger.v("sync delete and mark as deleted batch " + downloadBatchStatus.getDownloadBatchId().rawId());
            downloadBatchStatus.markAsDeleted();
            downloadsBatchPersistence.deleteSync(downloadBatchStatus);
            notifyCallback(callbackThrottle, downloadBatchStatus);
        }
    }

    private static void notifyCallback(CallbackThrottle callbackThrottle, InternalDownloadBatchStatus downloadBatchStatus) {
        callbackThrottle.update(downloadBatchStatus.copy());
    }

    private static boolean connectionNotAllowedForDownload(ConnectionChecker connectionChecker, DownloadBatchStatus.Status status) {
        return !connectionChecker.isAllowedToDownload() && status != DOWNLOADED;
    }

    private static void processNetworkError(InternalDownloadBatchStatus downloadBatchStatus,
                                            CallbackThrottle callbackThrottle,
                                            DownloadsBatchPersistence downloadsBatchPersistence) {
        if (downloadBatchStatus.status() == DELETING) {
            Logger.v("abort processNetworkError, the batch " + downloadBatchStatus.getDownloadBatchId().rawId() + " is deleting");
            return;
        }
        downloadBatchStatus.markAsWaitingForNetwork(downloadsBatchPersistence);
        notifyCallback(callbackThrottle, downloadBatchStatus);
        Logger.v(
                "scheduleRecovery for batch "
                        + downloadBatchStatus.getDownloadBatchId().rawId()
                        + ", "
                        + STATUS
                        + " " + downloadBatchStatus.status()
        );
        DownloadsNetworkRecoveryCreator.getInstance().scheduleRecovery();
    }

    private static void markAsDownloadingIfNeeded(InternalDownloadBatchStatus downloadBatchStatus,
                                                  DownloadsBatchPersistence downloadsBatchPersistence,
                                                  CallbackThrottle callbackThrottle) {
        if (downloadBatchStatus.status() != DOWNLOADED) {
            Logger.v("mark " + downloadBatchStatus.getDownloadBatchId().rawId() + " from " + downloadBatchStatus.status() + " to DOWNLOADING");
            downloadBatchStatus.markAsDownloading(downloadsBatchPersistence);
            notifyCallback(callbackThrottle, downloadBatchStatus);
        }
    }

    private static boolean shouldAbortAfterGettingTotalBatchSize(InternalDownloadBatchStatus downloadBatchStatus,
                                                                 DownloadsBatchPersistence downloadsBatchPersistence,
                                                                 CallbackThrottle callbackThrottle,
                                                                 long totalBatchSizeBytes) {
        if (downloadBatchStatus.status() == PAUSED) {
            notifyCallback(callbackThrottle, downloadBatchStatus);
            return true;
        }

        if (downloadBatchStatus.status() == DELETING) {
            deleteBatchIfNeeded(downloadBatchStatus, downloadsBatchPersistence, callbackThrottle);
            notifyCallback(callbackThrottle, downloadBatchStatus);
            return true;
        }

        if (totalBatchSizeBytes <= ZERO_BYTES) {
            processNetworkError(downloadBatchStatus, callbackThrottle, downloadsBatchPersistence);
            notifyCallback(callbackThrottle, downloadBatchStatus);
            return true;
        }

        return false;
    }

    private static boolean batchCannotContinue(InternalDownloadBatchStatus downloadBatchStatus,
                                               ConnectionChecker connectionChecker,
                                               DownloadsBatchPersistence downloadsBatchPersistence,
                                               CallbackThrottle callbackThrottle) {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();

        if (connectionNotAllowedForDownload(connectionChecker, status)) {
            downloadBatchStatus.markAsWaitingForNetwork(downloadsBatchPersistence);
            notifyCallback(callbackThrottle, downloadBatchStatus);
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
            downloadBatchStatus.updateDownloaded(currentBytesDownloaded);

            if (currentBytesDownloaded > totalBatchSizeBytes) {
                DownloadError downloadError = DownloadErrorFactory.createSizeMismatchError(downloadFileStatus);
                downloadBatchStatus.markAsError(Optional.of(downloadError), downloadsBatchPersistence);
                notifyCallback(callbackThrottle, downloadBatchStatus.copy());
                return;
            }

            if (currentBytesDownloaded == totalBatchSizeBytes && totalBatchSizeBytes != ZERO_BYTES) {
                downloadBatchStatus.markAsDownloaded(downloadsBatchPersistence);
            }

            if (downloadFileStatus.isMarkedAsError()) {
                downloadBatchStatus.markAsError(downloadFileStatus.error(), downloadsBatchPersistence);
            }

            if (downloadFileStatus.isMarkedAsWaitingForNetwork()) {
                downloadBatchStatus.markAsWaitingForNetwork(downloadsBatchPersistence);
            }

            notifyCallback(callbackThrottle, downloadBatchStatus.copy());
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
        if (downloadBatchStatus.status() == DELETING) {
            Logger.v("abort networkError check because the batch " + downloadBatchStatus.getDownloadBatchId().rawId() + " is deleting");
            return false;
        }
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status == WAITING_FOR_NETWORK) {
            return true;
        } else if (status == ERROR) {
            DownloadError downloadError = downloadBatchStatus.downloadError();
            return downloadError != null && downloadError.type() == DownloadError.Type.NETWORK_ERROR_CANNOT_DOWNLOAD_FILE;
        }
        return false;
    }

    void pause() {
        Logger.v("pause batch " + downloadBatchStatus.getDownloadBatchId().rawId() + ", " + STATUS + " " + downloadBatchStatus.status());
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status == PAUSED || status == DOWNLOADED) {
            return;
        }
        downloadBatchStatus.markAsPaused(downloadsBatchPersistence);
        notifyCallback(callbackThrottle, downloadBatchStatus);

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
        notifyCallback(callbackThrottle, downloadBatchStatus);
        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.resume();
        }
    }

    void delete() {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status == DELETING || status == DELETED) {
            Logger.v("abort delete batch " + downloadBatchStatus.getDownloadBatchId().rawId() + " because the " + STATUS + " is " + status);
            return;
        }

        downloadBatchStatus.markAsDeleting();
        Logger.v("delete request for batch " + downloadBatchStatus.getDownloadBatchId().rawId()
                         + ", " + STATUS + " " + downloadBatchStatus.status()
                         + ", should be deleting");
        notifyCallback(callbackThrottle, downloadBatchStatus);

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.delete();
        }

        if (status == PAUSED || status == DOWNLOADED || status == WAITING_FOR_NETWORK || status == ERROR) {
            Logger.v("delete async paused or downloaded batch " + downloadBatchStatus.getDownloadBatchId().rawId());
            downloadsBatchPersistence.deleteAsync(downloadBatchStatus, downloadBatchId -> {
                Logger.v("delete paused or downloaded mark as deleted: " + downloadBatchId.rawId());
                downloadBatchStatus.markAsDeleted();
                notifyCallback(callbackThrottle, downloadBatchStatus);
            });
        }

        Logger.v("delete request for batch end " + downloadBatchStatus.getDownloadBatchId().rawId()
                         + ", " + STATUS + ": " + downloadBatchStatus.status()
                         + ", should be deleting");
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

    @WorkerThread
    void updateTotalSize() {
        if (totalBatchSizeBytes == 0) {
            totalBatchSizeBytes = DownloadBatchSizeCalculator.getTotalSize(
                    downloadFiles,
                    downloadBatchStatus.status(),
                    downloadBatchStatus.getDownloadBatchId()
            );
        }
        downloadBatchStatus.updateTotalSize(totalBatchSizeBytes);
    }
}
