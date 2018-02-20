package com.novoda.downloadmanager;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class DownloadManager implements LiteDownloadManagerCommands {

    private final Object waitForDownloadService;
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
                    ExecutorService executor,
                    Handler callbackHandler,
                    Map<DownloadBatchId, DownloadBatch> downloadBatchMap,
                    List<DownloadBatchStatusCallback> callbacks,
                    FileOperations fileOperations,
                    DownloadsBatchPersistence downloadsBatchPersistence,
                    LiteDownloadManagerDownloader downloader,
                    ConnectionChecker connectionChecker) {
        this.waitForDownloadService = waitForDownloadService;
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
        // if device is connected to the internet
        // and the type of the connection is allowed
        // then start immediately
        // else, schedule a job with window 1ms-1day

        // if the job fails because of network issues, then schedule a job with window 1ms-1day

        downloader.download(batch, downloadBatchMap);
    }

    @Override
    public void pause(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return;
        }
        downloadBatch.pause();
    }

    @Override
    public void resume(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return;
        }

        if (downloadBatch.status().status() == DownloadBatchStatus.Status.DOWNLOADING) {
            return;
        }

        downloadBatchMap.remove(downloadBatchId);
        downloadBatch.resume();

        downloader.download(downloadBatch, downloadBatchMap);
    }

    @Override
    public void delete(DownloadBatchId downloadBatchId) {
        DownloadBatch downloadBatch = downloadBatchMap.get(downloadBatchId);
        if (downloadBatch == null) {
            return;
        }
        downloadBatchMap.remove(downloadBatchId);
        downloadBatch.delete();
    }

    @Override
    public void addDownloadBatchCallback(DownloadBatchStatusCallback downloadBatchCallback) {
        callbacks.add(downloadBatchCallback);
    }

    @Override
    public void removeDownloadBatchCallback(DownloadBatchStatusCallback downloadBatchCallback) {
        if (callbacks.contains(downloadBatchCallback)) {
            callbacks.remove(downloadBatchCallback);
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
    public DownloadFileStatus getDownloadStatusWithMatching(DownloadFileId downloadFileId) {
        return Wait.<DownloadFileStatus>waitFor(downloadService, waitForDownloadService)
                .thenPerform(() -> executeGetDownloadStatusWithMatching(downloadFileId));
    }

    @Nullable
    private DownloadFileStatus executeGetDownloadStatusWithMatching(DownloadFileId downloadFileId) {
        for (DownloadBatch downloadBatch : downloadBatchMap.values()) {
            DownloadFileStatus downloadFileStatus = downloadBatch.downloadFileStatusWith(downloadFileId);

            if (downloadFileStatus != null) {
                return downloadFileStatus;
            }
        }
        return null;
    }

    @Override
    public void getDownloadStatusWithMatching(DownloadFileId downloadFileId, DownloadFileStatusCallback callback) {
        executor.submit((Runnable) () -> Wait.<Void>waitFor(downloadService, waitForDownloadService)
                .thenPerform(() -> {
                    DownloadFileStatus downloadFileStatus = executeGetDownloadStatusWithMatching(downloadFileId);
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
            submitAllStoredDownloads(() -> Log.v("Allowed connectionType updated to " + allowedConnectionType + ". All jobs submitted"));
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
