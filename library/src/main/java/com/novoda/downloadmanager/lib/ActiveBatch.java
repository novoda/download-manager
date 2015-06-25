package com.novoda.downloadmanager.lib;

import java.util.List;

class ActiveBatch {

    private final long batchId;
    private final BatchInfo info;
    private final List<DownloadInfo> downloads;
    private final int status;

    public ActiveBatch(long batchId, BatchInfo info, List<DownloadInfo> downloads, int status) {
        this.batchId = batchId;
        this.info = info;
        this.downloads = downloads;
        this.status = status;
    }

    public long getBatchId() {
        return batchId;
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
}
