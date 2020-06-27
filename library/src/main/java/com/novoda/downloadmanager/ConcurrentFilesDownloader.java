package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ConcurrentFilesDownloader implements FilesDownloader {

    private static final ExecutorService CONCURRENT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

    private final InternalDownloadBatchStatus downloadBatchStatus;
    private final ConnectionChecker connectionChecker;
    private final DownloadsBatchPersistence downloadsBatchPersistence;

    ConcurrentFilesDownloader(InternalDownloadBatchStatus downloadBatchStatus, ConnectionChecker connectionChecker,
                              DownloadsBatchPersistence downloadsBatchPersistence) {
        this.downloadBatchStatus = downloadBatchStatus;
        this.connectionChecker = connectionChecker;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
    }

    @Override
    public void download(List<DownloadFile> downloadFiles, DownloadBatchStatusCallback statusCallback, DownloadFile.Callback fileCallback) {
        List<Callable<Object>> callables = new ArrayList<>(downloadFiles.size());

        for (DownloadFile downloadFile : downloadFiles) {
            callables.add(Executors.callable(() -> {
                if (DownloadBatch.batchCannotContinue(downloadBatchStatus, connectionChecker, downloadsBatchPersistence, statusCallback)) {
                    throw new CancellationException("Ignored interrupt download exception");
                }
                downloadFile.download(fileCallback);
            }));
        }
        try {
            CONCURRENT_EXECUTOR_SERVICE.invokeAll(callables);
        } catch (InterruptedException e) {
            CONCURRENT_EXECUTOR_SERVICE.shutdown();
        }
    }
}

