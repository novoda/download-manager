package com.novoda.downloadmanager;

interface DownloadService extends DownloadManagerService {

    void download(DownloadBatch downloadBatch, DownloadBatchStatusCallback callback);
}

