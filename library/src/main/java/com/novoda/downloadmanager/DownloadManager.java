package com.novoda.downloadmanager;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class DownloadManager implements LiteDownloadManagerCommands {

    private final Object waitForDownloadService;
    private final ExecutorService executor;
    private final Handler callbackHandler;
    private final Map<DownloadBatchId, DownloadBatch> downloadBatchMap;
    private final List<DownloadBatchCallback> callbacks;
    private final FileOperations fileOperations;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final LiteDownloadManagerDownloader downloader;

    private DownloadService downloadService;

    // DownloadManager is a complex object.
    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
    DownloadManager(Object waitForDownloadService,
                    ExecutorService executor,
                    Handler callbackHandler,
                    Map<DownloadBatchId, DownloadBatch> downloadBatchMap,
                    List<DownloadBatchCallback> callbacks,
                    FileOperations fileOperations,
                    DownloadsBatchPersistence downloadsBatchPersistence,
                    LiteDownloadManagerDownloader downloader) {
        this.waitForDownloadService = waitForDownloadService;
        this.executor = executor;
        this.callbackHandler = callbackHandler;
        this.downloadBatchMap = downloadBatchMap;
        this.callbacks = callbacks;
        this.fileOperations = fileOperations;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloader = downloader;
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
    public void addDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback) {
        callbacks.add(downloadBatchCallback);
    }

    @Override
    public void removeDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback) {
        if (callbacks.contains(downloadBatchCallback)) {
            callbacks.remove(downloadBatchCallback);
        }
    }

    @WorkerThread
    @Override
    public List<DownloadBatchStatus> getAllDownloadBatchStatuses() {
        if (downloadService == null) {
            try {
                synchronized (waitForDownloadService) {
                    waitForDownloadService.wait();
                }
            } catch (InterruptedException e) {
                Log.e(e, "Interrupted waiting for download service.");
            }
        }
        return executeGetAllDownloadBatchStatuses();
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
        if (downloadService == null) {
            executor.submit(
                    WaitForLockRunnable.waitFor(waitForDownloadService)
                            .thenPerform(() -> executeGetAllDownloadBatchStatuses(callback))
            );
        } else {
            executeGetAllDownloadBatchStatuses(callback);
        }
    }

    private void executeGetAllDownloadBatchStatuses(AllBatchStatusesCallback callback) {
        List<DownloadBatchStatus> downloadBatchStatuses = executeGetAllDownloadBatchStatuses();
        callbackHandler.post(() -> callback.onReceived(downloadBatchStatuses));
    }

    @WorkerThread
    @Override
    public DownloadFileStatus getDownloadStatusWithMatching(DownloadFileId downloadFileId) {
        if (downloadService == null) {
            try {
                synchronized (waitForDownloadService) {
                    waitForDownloadService.wait();
                }
            } catch (InterruptedException e) {
                Log.e(e, "Interrupted waiting for download service.");
            }
        }
        return executeFirstLocalPathForDownloadMatching(downloadFileId);
    }

    @Nullable
    private DownloadFileStatus executeFirstLocalPathForDownloadMatching(DownloadFileId downloadFileId) {
        for (DownloadBatch downloadBatch : downloadBatchMap.values()) {
            DownloadFile downloadFile = downloadBatch.downloadFileWith(downloadFileId);

            if (downloadFile != null) {
                return downloadFile.fileStatus();
            }
        }
        return null;
    }

    @Override
    public void getDownloadStatusWithMatching(DownloadFileId downloadFileId, DownloadFileStatusCallback callback) {
        if (downloadService == null) {
            executor.submit(
                    WaitForLockRunnable.waitFor(waitForDownloadService)
                            .thenPerform(() -> executeFirstLocalPathForDownloadWithMatching(downloadFileId, callback))
            );
        } else {
            executeFirstLocalPathForDownloadWithMatching(downloadFileId, callback);
        }
    }

    private void executeFirstLocalPathForDownloadWithMatching(DownloadFileId downloadFileId, DownloadFileStatusCallback callback) {
        DownloadFileStatus downloadFileStatus = executeFirstLocalPathForDownloadMatching(downloadFileId);
        callbackHandler.post(() -> callback.onReceived(downloadFileStatus));
    }

}
