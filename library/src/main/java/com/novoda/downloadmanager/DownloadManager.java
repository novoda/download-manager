package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class DownloadManager implements LiteDownloadManagerCommands {

    private final Object waitForDownloadService;
    private final ExecutorService executor;
    private final Map<DownloadBatchId, DownloadBatch> downloadBatchMap;
    private final List<DownloadBatchCallback> callbacks;
    private final FileOperations fileOperations;
    private final DownloadsBatchPersistence downloadsBatchPersistence;
    private final LiteDownloadManagerDownloader downloader;

    private DownloadService downloadService;

    DownloadManager(Object waitForDownloadService,
                    ExecutorService executor,
                    Map<DownloadBatchId, DownloadBatch> downloadBatchMap,
                    List<DownloadBatchCallback> callbacks,
                    FileOperations fileOperations,
                    DownloadsBatchPersistence downloadsBatchPersistence,
                    LiteDownloadManagerDownloader downloader) {
        this.waitForDownloadService = waitForDownloadService;
        this.executor = executor;
        this.downloadBatchMap = downloadBatchMap;
        this.callbacks = callbacks;
        this.fileOperations = fileOperations;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
        this.downloader = downloader;
    }

    void initialise(DownloadService downloadService) {
        setDownloadService(downloadService);
    }

    private void setDownloadService(DownloadService downloadService) {
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

    private DownloadsBatchPersistence.LoadBatchesCallback loadBatchesCallback(final AllStoredDownloadsSubmittedCallback callback) {
        return new DownloadsBatchPersistence.LoadBatchesCallback() {
            @Override
            public void onLoaded(List<DownloadBatch> downloadBatches) {
                for (DownloadBatch downloadBatch : downloadBatches) {
                    downloader.download(downloadBatch, downloadBatchMap);
                }

                callback.onAllDownloadsSubmitted();
            }
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

    @Override
    public void getAllDownloadBatchStatuses(AllBatchStatusesCallback callback) {
        if (downloadService == null) {
            ensureDownloadServiceExistsAndGetAllDownloadBatchStatuses(callback);
        } else {
            executeGetAllDownloadBatchStatuses(callback);
        }
    }

    private void ensureDownloadServiceExistsAndGetAllDownloadBatchStatuses(final AllBatchStatusesCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                waitForDownloadService();
                executeGetAllDownloadBatchStatuses(callback);
            }
        });
    }

    private void waitForDownloadService() {
        try {
            synchronized (waitForDownloadService) {
                waitForDownloadService.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeGetAllDownloadBatchStatuses(AllBatchStatusesCallback callback) {
        List<DownloadBatchStatus> downloadBatchStatuses = new ArrayList<>(downloadBatchMap.size());

        for (DownloadBatch downloadBatch : downloadBatchMap.values()) {
            downloadBatchStatuses.add(downloadBatch.status());
        }

        callback.onReceived(downloadBatchStatuses);
    }
}
