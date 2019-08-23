package com.novoda.downloadmanager;

import java.io.File;

final class BatchStorageRoot {

    static BatchStorageRoot with(StorageRoot storageRoot, DownloadBatchId downloadBatchId) {
        return new BatchStorageRoot(storageRoot, downloadBatchId);
    }

    private final String path;

    private BatchStorageRoot(StorageRoot storageRootPath, DownloadBatchId downloadBatchId) {
        path = storageRootPath.path() + File.separator + downloadBatchId.rawId();
    }

    public String path() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BatchStorageRoot batchStorageRoot = (BatchStorageRoot) o;

        return (path != null ? path.equals(batchStorageRoot.path) : batchStorageRoot.path == null);
    }

    @Override
    public int hashCode() {
        return (path != null ? path.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "BatchStorageRoot{"
                + "path='" + path + "\'}";
    }

}
