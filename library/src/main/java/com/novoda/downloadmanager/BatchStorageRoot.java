package com.novoda.downloadmanager;

import java.io.File;

class BatchStorageRoot {

    static BatchStorageRoot with(StorageRoot storageRoot, DownloadBatchId downloadBatchId) {
        return new BatchStorageRoot(storageRoot, downloadBatchId);
    }

    private String path;

    private BatchStorageRoot(StorageRoot storageRootPath, DownloadBatchId downloadBatchId) {
        path = storageRootPath.path() + File.separator + downloadBatchId.rawId();
    }

    public String path() {
        return path;
    }

}