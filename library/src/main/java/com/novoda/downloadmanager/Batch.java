package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

public class Batch {

    private final StorageRoot storageRoot;
    private final DownloadBatchId downloadBatchId;
    private final String title;
    private final List<BatchFile> batchFiles;

    public static BatchBuilder with(StorageRoot storageRoot, DownloadBatchId downloadBatchId, String title) {
        return new LiteBatchBuilder(storageRoot, downloadBatchId, title, new ArrayList<>());
    }

    Batch(StorageRoot storageRoot, DownloadBatchId downloadBatchId, String title, List<BatchFile> batchFiles) {
        this.storageRoot = storageRoot;
        this.downloadBatchId = downloadBatchId;
        this.title = title;
        this.batchFiles = batchFiles;
    }

    public DownloadBatchId downloadBatchId() {
        return downloadBatchId;
    }

    public String title() {
        return title;
    }

    public List<BatchFile> batchFiles() {
        return batchFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Batch batch = (Batch) o;

        if (storageRoot != null ? !storageRoot.equals(batch.storageRoot) : batch.storageRoot != null) {
            return false;
        }
        if (downloadBatchId != null ? !downloadBatchId.equals(batch.downloadBatchId) : batch.downloadBatchId != null) {
            return false;
        }
        if (title != null ? !title.equals(batch.title) : batch.title != null) {
            return false;
        }
        return batchFiles != null ? batchFiles.equals(batch.batchFiles) : batch.batchFiles == null;
    }

    @Override
    public int hashCode() {
        int result = storageRoot != null ? storageRoot.hashCode() : 0;
        result = 31 * result + (downloadBatchId != null ? downloadBatchId.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (batchFiles != null ? batchFiles.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Batch{"
                + "storageRoot=" + storageRoot
                + ", downloadBatchId=" + downloadBatchId
                + ", title='" + title + '\''
                + ", batchFiles=" + batchFiles
                + '}';
    }
}
