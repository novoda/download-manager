package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

public class CompletedDownloadBatch {

    private final DownloadBatchId downloadBatchId;
    private final DownloadBatchTitle downloadBatchTitle;
    private final long downloadedDateTimeInMillis;
    private final List<CompletedDownloadFile> completedDownloadFiles;
    private final StorageRoot storageRoot;

    public CompletedDownloadBatch(DownloadBatchId downloadBatchId,
                                  DownloadBatchTitle downloadBatchTitle,
                                  long downloadedDateTimeInMillis,
                                  List<CompletedDownloadFile> completedDownloadFiles,
                                  StorageRoot storageRoot) {
        this.downloadBatchId = downloadBatchId;
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        this.completedDownloadFiles = completedDownloadFiles;
        this.storageRoot = storageRoot;
    }

    public DownloadBatchId downloadBatchId() {
        return downloadBatchId;
    }

    public DownloadBatchTitle downloadBatchTitle() {
        return downloadBatchTitle;
    }

    public long downloadedDateTimeInMillis() {
        return downloadedDateTimeInMillis;
    }

    public List<CompletedDownloadFile> completedDownloadFiles() {
        return completedDownloadFiles;
    }

    public StorageRoot storageRoot() {
        return storageRoot;
    }

    public Batch asBatch() {
        return new Batch(
                storageRoot,
                downloadBatchId,
                downloadBatchTitle.asString(),
                asBatchFiles()
        );
    }

    private List<BatchFile> asBatchFiles() {
        List<BatchFile> batchFiles = new ArrayList<>(completedDownloadFiles.size());
        for (CompletedDownloadFile completedDownloadFile : completedDownloadFiles) {
            batchFiles.add(completedDownloadFile.asBatchFile());
        }
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

        CompletedDownloadBatch that = (CompletedDownloadBatch) o;

        if (downloadedDateTimeInMillis != that.downloadedDateTimeInMillis) {
            return false;
        }
        if (downloadBatchId != null ? !downloadBatchId.equals(that.downloadBatchId) : that.downloadBatchId != null) {
            return false;
        }
        if (downloadBatchTitle != null ? !downloadBatchTitle.equals(that.downloadBatchTitle) : that.downloadBatchTitle != null) {
            return false;
        }
        if (completedDownloadFiles != null ? !completedDownloadFiles.equals(that.completedDownloadFiles) : that.completedDownloadFiles != null) {
            return false;
        }
        return storageRoot != null ? storageRoot.equals(that.storageRoot) : that.storageRoot == null;
    }

    @Override
    public int hashCode() {
        int result = downloadBatchId != null ? downloadBatchId.hashCode() : 0;
        result = 31 * result + (downloadBatchTitle != null ? downloadBatchTitle.hashCode() : 0);
        result = 31 * result + (int) (downloadedDateTimeInMillis ^ (downloadedDateTimeInMillis >>> 32));
        result = 31 * result + (completedDownloadFiles != null ? completedDownloadFiles.hashCode() : 0);
        result = 31 * result + (storageRoot != null ? storageRoot.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompletedDownloadBatch{"
                + "downloadBatchId=" + downloadBatchId
                + ", downloadBatchTitle=" + downloadBatchTitle
                + ", downloadedDateTimeInMillis=" + downloadedDateTimeInMillis
                + ", completedDownloadFiles=" + completedDownloadFiles
                + ", storageRoot=" + storageRoot
                + '}';
    }

}
