package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

public class Batch {

    private final DownloadBatchId downloadBatchId;
    private final String title;
    private final List<BatchFile> batchFiles;

    public static BatchBuilder with(DownloadBatchId downloadBatchId, String title) {
        return new LiteBatchBuilder(downloadBatchId, title, new ArrayList<>());
    }

    Batch(DownloadBatchId downloadBatchId, String title, List<BatchFile> batchFiles) {
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
        int result = downloadBatchId != null ? downloadBatchId.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (batchFiles != null ? batchFiles.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Batch{"
                + "downloadBatchId=" + downloadBatchId
                + ", title='" + title + '\''
                + ", batchFiles=" + batchFiles
                + '}';
    }

}
