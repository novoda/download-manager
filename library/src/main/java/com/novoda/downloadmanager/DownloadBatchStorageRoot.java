package com.novoda.downloadmanager;

import java.io.File;

class DownloadBatchStorageRoot {


    static DownloadBatchStorageRoot with(StorageRoot storageRoot, DownloadBatchId downloadBatchId) {
        return new DownloadBatchStorageRoot(storageRoot, downloadBatchId);
    }

    private StorageRoot storageRootPath;
    private DownloadBatchId downloadBatchId;

    private DownloadBatchStorageRoot(StorageRoot storageRootPath, DownloadBatchId downloadBatchId) {
        this.storageRootPath = storageRootPath;
        this.downloadBatchId = downloadBatchId;
    }

    String getBatchStorageRootPath() {
        return storageRootPath.path() + File.separator + downloadBatchId.rawId();
    }

    StorageRoot getStorageRootPath() {
        return storageRootPath;
    }

    public DownloadBatchId getDownloadBatchId() {
        return downloadBatchId;
    }
}