package com.novoda.downloadmanager;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class DownloadManager implements LiteDownloadManagerCommands {

    private final Object waitForDownloadService;
    private final Object waitForDownloadBatchStatusCallback;
    private final ExecutorService executor;
    private final Handler callbackHandler;
    private final Map<DownloadBatchId, DownloadBatch> downloadBatchMap;
    private final List<DownloadBatchStatusCallback> callbacks;
    private final FileOperations fileOperations;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final LiteDownloadManagerDownloader downloader;
    private final ConnectionChecker connectionChecker;

    private DownloadService downloadService;

    // DownloadManager is a complex object.
    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
    DownloadManager(Object waitForDownloadService,
                    Object waitForDownloadBatchStatusCallback,
                    ExecutorService executor,
                    Handler callbackHandler,
                    Map<DownloadBatchId, DownloadBatch> downloadBatchMap,
                    List<DownloadBatchStatusCallback> callbacks,
                    FileOperations fileOperations,
                    DownloadsBatchPersistence downloadsBatchPersistence,
                    LiteDownloadManagerDownloader downloader,
                    ConnectionChecker connectionChecker) {
        this.waitForDownloadService = waitForDownloadService;
        this.waitForDownloadBatchStatusCallback = waitForDownloadBatchStatusCallback;
        this.executor = executor;
        this.callbackHandler = callbackHandler;
        this.downloadBatchMap = downloadBatchMap;
        this.callbacks = callbacks;
        this.fileOperations = fileOperations;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloader = downloader;
        this.connectionChecker = connectionChecker;
    }

    void initialise(DownloadService downloadService) {
        this.downloadService = downloadService;
        downloader.setDownloadService(downloadService);
        synchronized (waitForDownloadService) {
            waitForDownloadService.notifyAll();
        }
    }

    @Override
    public void submitAllStoredDownloads(AllStoredDownloadsSubmittedCallback callback) {
        downloadsBatchPersistence.loadAsync(fileOperations, loadBatchesCallback(callback));
    }

    private DownloadsBatchPersistence.LoadBatchesCallback loadBatchesCallback(AllStoredDownloadsSubmittedCallback callback) {
        return downloadBatches -> {
            for (DownloadBatch downloadBatch : downloadBatches) {
                downloader.download(downloadBatch, downloadBatchMap);
            }

            callbackHandler.post(callback::onAllDownloadsSubmitted);
        };
    }

    @Override
    public void download(Batch batch) {
        DownloadBatchId downloadBatchId = batch.downloadBatchId();
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            downloader.download(batch, downloadBatchMap);
        } else {
            Logger.v("abort download batch " + downloadBatchId + " will not download as exists already in the running batches map");
        }
    }

    @Override
    public void pause(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            Logger.v("abort pause batch " + downloadBatchId + " will not be paused as it does not exists in the running batches map");
            return;
        }
        downloadBatch.pause();
    }

    @Override
    public void resume(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            Logger.v("abort resume batch " + downloadBatchId + " will not be resume as it does not exists in the running batches map");
            return;
        }

        if (downloadBatch.status().status() == DownloadBatchStatus.Status.DOWNLOADING) {
            Logger.v("abort resume batch " + downloadBatchId + " will not be resume as it's already downloading");
            return;
        }

        downloadBatch.resume();
        downloader.download(downloadBatch, downloadBatchMap);
    }

    @Override
    public void delete(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            Logger.v("abort delete batch " + downloadBatchId + " will not be deleted as it does not exists in the running batches map");
            return;
        }

        downloadBatch.delete();
    }

    @Override
    public void addDownloadBatchCallback(DownloadBatchStatusCallback downloadBatchCallback) {
        synchronized (waitForDownloadBatchStatusCallback) {
            callbacks.add(downloadBatchCallback);
        }
    }

    @Override
    public void removeDownloadBatchCallback(DownloadBatchStatusCallback downloadBatchCallback) {
        synchronized (waitForDownloadBatchStatusCallback) {
            if (callbacks.contains(downloadBatchCallback)) {
                callbacks.remove(downloadBatchCallback);
            }
        }
    }

    @WorkerThread
    @Override
    public List<DownloadBatchStatus> getAllDownloadBatchStatuses() {
        return Wait.<List<DownloadBatchStatus>>waitFor(downloadService, waitForDownloadService)
                .thenPerform(this::executeGetAllDownloadBatchStatuses);
    }

    private List<DownloadBatchStatus> executeGetAllDownloadBatchStatuses() {
        List<DownloadBatchStatus> downloadBatchStatuses = new ArrayList<>(downloadBatchMap.size());

        for (DownloadBatch downloadBatch : downloadBatchMap.values()) {
            downloadBatchStatuses.add(downloadBatch.status());
        }
        return downloadBatchStatuses;
    }

    @Override
    public void getAllDownloadBatchStatuses(AllBatchStatusesCallback callback) {
        executor.submit((Runnable) () -> Wait.<Void>waitFor(downloadService, waitForDownloadService)
                .thenPerform(() -> {
                    List<DownloadBatchStatus> downloadBatchStatuses = executeGetAllDownloadBatchStatuses();
                    callbackHandler.post(() -> callback.onReceived(downloadBatchStatuses));
                    return null;
                }));
    }

    @Nullable
    @WorkerThread
    @Override
    public DownloadFileStatus getDownloadFileStatusWithMatching(DownloadBatchId downloadBatchId, DownloadFileId downloadFileId) {
        return Wait.<DownloadFileStatus>waitFor(downloadService, waitForDownloadService)
                .thenPerform(() -> executeGetDownloadStatusWithMatching(downloadBatchId, downloadFileId));
    }

    @Nullable
    private DownloadFileStatus executeGetDownloadStatusWithMatching(DownloadBatchId downloadBatchId, DownloadFileId downloadFileId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return null;
        }

        DownloadFileStatus downloadFileStatus = downloadBatch.downloadFileStatusWith(downloadFileId);
        if (downloadFileStatus == null) {
            return null;
        }

        return downloadFileStatus;
    }

    @Override
    public void getDownloadFileStatusWithMatching(DownloadBatchId downloadBatchId,
                                                  DownloadFileId downloadFileId,
                                                  DownloadFileStatusCallback callback) {
        executor.submit((Runnable) () -> Wait.<Void>waitFor(downloadService, waitForDownloadService)
                .thenPerform(() -> {
                    DownloadFileStatus downloadFileStatus = executeGetDownloadStatusWithMatching(downloadBatchId, downloadFileId);
                    callbackHandler.post(() -> callback.onReceived(downloadFileStatus));
                    return null;
                }));
    }

    @Override
    public void updateAllowedConnectionType(ConnectionType allowedConnectionType) {
        if (allowedConnectionType == null) {
            throw new IllegalArgumentException("Allowed connection type cannot be null");
        }
        connectionChecker.updateAllowedConnectionType(allowedConnectionType);
        DownloadsNetworkRecoveryCreator.getInstance().updateAllowedConnectionType(allowedConnectionType);

        if (connectionChecker.isAllowedToDownload()) {
            submitAllStoredDownloads(() -> Logger.v("Allowed connectionType updated to " + allowedConnectionType + ". All jobs submitted"));
        } else {
            for (DownloadBatch downloadBatch : downloadBatchMap.values()) {
                downloadBatch.waitForNetwork();
            }
        }
    }

    @Override
    public File getDownloadsDir() {
        FilePersistence filePersistence = fileOperations.filePersistenceCreator().create();
        FilePath filePath = filePersistence.basePath();
        return new File(filePath.path());
    }

}
