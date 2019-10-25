package com.novoda.downloadmanager;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.File;
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
import static com.novoda.downloadmanager.DownloadError.Type.REQUIREMENT_RULE_VIOLATED;

// This model knows how to interact with low level components.
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
class DownloadBatch {

    private static final int ZERO_BYTES = 0;
    private static final String STATUS = ", status ";
    private static final String BATCH = "batch ";

    private final Map<DownloadFileId, Long> fileBytesDownloadedMap;
    private final InternalDownloadBatchStatus downloadBatchStatus;
    private final List<DownloadFile> downloadFiles;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final FileCallbackThrottle fileCallbackThrottle;
    private final ConnectionChecker connectionChecker;
    private final DownloadBatchRequirementRule downloadBatchRequirementRule;

    private long totalBatchSizeBytes;
    private DownloadBatchStatusCallback callback;

    DownloadBatch(InternalDownloadBatchStatus internalDownloadBatchStatus,
                  List<DownloadFile> downloadFiles,
                  Map<DownloadFileId, Long> fileBytesDownloadedMap,
                  DownloadsBatchPersistence downloadsBatchPersistence,
                  FileCallbackThrottle fileCallbackThrottle,
                  ConnectionChecker connectionChecker,
                  DownloadBatchRequirementRule downloadBatchRequirementRule
    ) {
        this.downloadFiles = downloadFiles;
        this.fileBytesDownloadedMap = fileBytesDownloadedMap;
        this.downloadBatchStatus = internalDownloadBatchStatus;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.fileCallbackThrottle = fileCallbackThrottle;
        this.connectionChecker = connectionChecker;
        this.downloadBatchRequirementRule = downloadBatchRequirementRule;
    }

    void setCallback(DownloadBatchStatusCallback callback) {
        this.callback = callback;
        fileCallbackThrottle.setCallback(callback);
    }

    void download() {
        String rawBatchId = downloadBatchStatus.getDownloadBatchId().rawId();
        Logger.v("start sync download " + rawBatchId + STATUS + downloadBatchStatus.status());

        if (shouldAbortStartingBatch(connectionChecker, callback, downloadBatchStatus, downloadsBatchPersistence)) {
            Logger.v("abort starting download " + rawBatchId + STATUS + downloadBatchStatus.status());
            return;
        }

        markAsDownloadingIfNeeded(downloadBatchStatus, downloadsBatchPersistence, callback);

        updateTotalSize();

        Logger.v(BATCH + downloadBatchStatus.getDownloadBatchId().rawId()
                         + STATUS + downloadBatchStatus.status()
                         + " totalBatchSize " + totalBatchSizeBytes);

        if (shouldAbortAfterGettingTotalBatchSize(
                downloadBatchStatus,
                downloadsBatchPersistence,
                callback,
                downloadBatchRequirementRule,
                totalBatchSizeBytes
        )) {
            Logger.v("abort after getting total " + BATCH + "size download " + rawBatchId + STATUS + downloadBatchStatus.status());
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
        fileCallbackThrottle.stopUpdates();
        Logger.v("end sync download " + rawBatchId);
    }

    private static boolean shouldAbortStartingBatch(ConnectionChecker connectionChecker,
                                                    DownloadBatchStatusCallback callback,
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
            deleteBatchIfNeeded(downloadBatchStatus, downloadsBatchPersistence, callback);
            notifyCallback(callback, downloadBatchStatus);
            return true;
        }

        if (downloadBatchStatus.status() == PAUSED) {
            notifyCallback(callback, downloadBatchStatus);
            return true;
        }

        if (connectionNotAllowedForDownload(connectionChecker, downloadBatchStatus.status())) {
            processNetworkError(downloadBatchStatus, callback, downloadsBatchPersistence);
            notifyCallback(callback, downloadBatchStatus);
            return true;
        }

        if (downloadBatchStatus.status() == DOWNLOADED) {
            notifyCallback(callback, downloadBatchStatus);
            return true;
        }

        return false;
    }

    private static void deleteBatchIfNeeded(InternalDownloadBatchStatus downloadBatchStatus,
                                            DownloadsBatchPersistence downloadsBatchPersistence,
                                            DownloadBatchStatusCallback callback) {
        if (downloadBatchStatus.status() == DELETING) {
            Logger.v("sync delete and mark as deleted " + BATCH + downloadBatchStatus.getDownloadBatchId().rawId());
            downloadBatchStatus.markAsDeleted();
            downloadsBatchPersistence.deleteSync(downloadBatchStatus);
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
            Logger.v("abort processNetworkError, the " + BATCH + downloadBatchStatus.getDownloadBatchId().rawId() + " is deleting");
            return;
        }
        downloadBatchStatus.markAsWaitingForNetwork(downloadsBatchPersistence);
        notifyCallback(callback, downloadBatchStatus);
        Logger.v(
                "scheduleRecovery for " + BATCH
                        + downloadBatchStatus.getDownloadBatchId().rawId()
                        + STATUS
                        + downloadBatchStatus.status()
        );
        DownloadsNetworkRecoveryCreator.getInstance().scheduleRecovery();
    }

    private static void markAsDownloadingIfNeeded(InternalDownloadBatchStatus downloadBatchStatus,
                                                  DownloadsBatchPersistence downloadsBatchPersistence,
                                                  DownloadBatchStatusCallback callback) {
        if (downloadBatchStatus.status() != DOWNLOADED) {
            Logger.v("mark " + downloadBatchStatus.getDownloadBatchId().rawId() + " from " + downloadBatchStatus.status() + " to DOWNLOADING");
            downloadBatchStatus.markAsDownloading(downloadsBatchPersistence);
            notifyCallback(callback, downloadBatchStatus);
        }
    }

    private static boolean shouldAbortAfterGettingTotalBatchSize(InternalDownloadBatchStatus downloadBatchStatus,
                                                                 DownloadsBatchPersistence downloadsBatchPersistence,
                                                                 DownloadBatchStatusCallback callback,
                                                                 DownloadBatchRequirementRule downloadBatchRequirementRule,
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

        if (downloadBatchRequirementRule.hasViolatedRule(downloadBatchStatus)) {
            Optional<DownloadError> error = Optional.fromNullable(new DownloadError(REQUIREMENT_RULE_VIOLATED));
            downloadBatchStatus.markAsError(error, downloadsBatchPersistence);
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
            downloadBatchStatus.updateDownloaded(currentBytesDownloaded);

            if (currentBytesDownloaded > totalBatchSizeBytes) {
                DownloadError downloadError = DownloadErrorFactory.createSizeMismatchError(downloadFileStatus);
                downloadBatchStatus.markAsError(Optional.of(downloadError), downloadsBatchPersistence);
                fileCallbackThrottle.update(downloadBatchStatus);
                Logger.e("Abort fileDownloadCallback: " + downloadError.message());
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

            fileCallbackThrottle.update(downloadBatchStatus);
        }

        @Override
        public void onDelete() {
            deleteDownloadDirectories();
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
        if (downloadBatchStatus.status() == DELETING) {
            Logger.v(BATCH + downloadBatchStatus.getDownloadBatchId().rawId()
                             + STATUS + status
                             + " abort network error");
            return false;
        }
        if (status == WAITING_FOR_NETWORK) {
            return true;
        } else if (status == ERROR) {
            DownloadError downloadError = downloadBatchStatus.downloadError();
            return downloadError != null && downloadError.type() == DownloadError.Type.NETWORK_ERROR_CANNOT_DOWNLOAD_FILE;
        }
        return false;
    }

    void pause() {
        Logger.v("pause " + BATCH + downloadBatchStatus.getDownloadBatchId().rawId() + STATUS + downloadBatchStatus.status());
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status == PAUSED || status == DOWNLOADED) {
            Logger.v(BATCH + downloadBatchStatus.getDownloadBatchId().rawId()
                             + STATUS + status
                             + " abort pause batch");
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
            Logger.v(BATCH + downloadBatchStatus.getDownloadBatchId().rawId()
                             + STATUS + status
                             + " abort wait for network");
            return;
        }

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.waitForNetwork();
        }
    }

    void resume() {
        DownloadBatchStatus.Status status = downloadBatchStatus.status();
        if (status == QUEUED || status == DOWNLOADING || status == DOWNLOADED) {
            Logger.v(BATCH + downloadBatchStatus.getDownloadBatchId().rawId()
                             + STATUS + status
                             + " abort resume batch");
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
            Logger.v(BATCH + downloadBatchStatus.getDownloadBatchId().rawId()
                             + STATUS + status
                             + " abort delete batch");
            return;
        }

        downloadBatchStatus.markAsDeleting();
        Logger.v("delete request for " + BATCH + downloadBatchStatus.getDownloadBatchId().rawId()
                         + STATUS + status
                         + ", should be deleting");
        notifyCallback(callback, downloadBatchStatus);

        for (DownloadFile downloadFile : downloadFiles) {
            downloadFile.delete();
        }

        deleteDownloadDirectories();

        if (status == PAUSED || status == DOWNLOADED || status == WAITING_FOR_NETWORK || status == ERROR) {
            Logger.v("delete async paused or downloaded " + BATCH + downloadBatchStatus.getDownloadBatchId().rawId());
            downloadsBatchPersistence.deleteAsync(downloadBatchStatus, downloadBatchId -> {
                Logger.v("delete paused or downloaded mark as deleted: " + downloadBatchId.rawId());
                downloadBatchStatus.markAsDeleted();
                notifyCallback(callback, downloadBatchStatus);
            });
        }

        Logger.v("delete request for " + BATCH + "end " + downloadBatchStatus.getDownloadBatchId().rawId()
                         + STATUS + status
                         + ", should be deleting");
    }

    private void deleteDownloadDirectories() {
        BatchStorageRoot batchStorageRoot = BatchStorageRoot.with(downloadBatchStatus::storageRoot, downloadBatchStatus.getDownloadBatchId());
        File batchRootDir = new File(batchStorageRoot.path());
        if (batchRootDir.exists()) {
            deleteDirectoriesIfEmpty(batchRootDir);
        }
    }

    private void deleteDirectoriesIfEmpty(File batchRootDirectory) {
        if (batchRootDirectory.isDirectory()) {
            File[] nestedDirectories = batchRootDirectory.listFiles();
            if (nestedDirectories != null) {
                for (File child : nestedDirectories) {
                    deleteDirectoriesIfEmpty(child);
                }
            }
        }

        if (isDirectoryEmpty(batchRootDirectory)) {
            boolean deleted = batchRootDirectory.delete();
            String message = String.format("File or Directory: %s deleted: %s", batchRootDirectory.getAbsolutePath(), deleted);
            Logger.d(getClass().getSimpleName(), message);
        }
    }

    private boolean isDirectoryEmpty(File directory) {
        if (directory.isDirectory()) {
            String[] children = directory.list();
            return children == null || children.length == 0;
        }
        return false;
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
                downloadBatchStatus.notificationSeen(),
                downloadBatchStatus.storageRoot()
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
                downloadBatchStatus.notificationSeen(),
                downloadBatchStatus.storageRoot()
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
