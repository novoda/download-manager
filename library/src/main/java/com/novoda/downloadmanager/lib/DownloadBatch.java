package com.novoda.downloadmanager.lib;

import java.util.List;

class DownloadBatch {

    public static final DownloadBatch DELETED = new DownloadBatch(-1, null, null, -1, -1L, -1L);

    private final long batchId;
    private final BatchInfo info;
    private final List<DownloadInfo> downloads;
    private final int status;
    private final long totalSizeBytes;
    private final long currentSizeBytes;

    public DownloadBatch(long batchId, BatchInfo info, List<DownloadInfo> downloads, int status, long totalSizeBytes, long currentSizeBytes) {
        this.batchId = batchId;
        this.info = info;
        this.downloads = downloads;
        this.status = status;
        this.totalSizeBytes = totalSizeBytes;
        this.currentSizeBytes = currentSizeBytes;
    }

    public long getBatchId() {
        return batchId;
    }

    public long getTotalSize() {
        return totalSizeBytes;
    }

    public long getCurrentSize() {
        return currentSizeBytes;
    }

    public BatchInfo getInfo() {
        return info;
    }

    public List<DownloadInfo> getDownloads() {
        return downloads;
    }

    public int getStatus() {
        return status;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DownloadBatch that = (DownloadBatch) o;
        return batchId == that.batchId;

    }

    @Override
    public int hashCode() {
        return (int) (batchId ^ (batchId >>> 32));
    }
}
