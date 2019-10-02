package com.novoda.downloadmanager;

import java.util.List;

class SequentialFilesDownloader implements FilesDownloader {

    private final InternalDownloadBatchStatus downloadBatchStatus;
    private final ConnectionChecker connectionChecker;
    private final DownloadsBatchPersistence downloadsBatchPersistence;

    SequentialFilesDownloader(InternalDownloadBatchStatus downloadBatchStatus, ConnectionChecker connectionChecker,
                              DownloadsBatchPersistence downloadsBatchPersistence) {
        this.downloadBatchStatus = downloadBatchStatus;
        this.connectionChecker = connectionChecker;
        this.downloadsBatchPersistence = downloadsBatchPersistence;
    }

    @Override
    public void download(List<DownloadFile> downloadFiles, DownloadBatchStatusCallback statusCallback, DownloadFile.Callback fileCallback) {
        for (DownloadFile downloadFile : downloadFiles) {
            if (DownloadBatch.batchCannotContinue(downloadBatchStatus, connectionChecker, downloadsBatchPersistence, statusCallback)) {
                break;
            }
            downloadFile.download(fileCallback);
        }
    }
}

