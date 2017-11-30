package com.novoda.downloadmanager;

public class LiteDownloadsBatchPersisted implements DownloadsBatchPersisted {

    private final DownloadBatchTitle downloadBatchTitle;
    private final DownloadBatchId downloadBatchId;
    private final DownloadBatchStatus.Status status;

    public LiteDownloadsBatchPersisted(DownloadBatchTitle downloadBatchTitle, DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status) {
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadBatchId = downloadBatchId;
        this.status = status;
    }

    @Override
    public DownloadBatchId downloadBatchId() {
        return downloadBatchId;
    }

    @Override
    public DownloadBatchStatus.Status downloadBatchStatus() {
        return status;
    }

    @Override
    public DownloadBatchTitle downloadBatchTitle() {
        return downloadBatchTitle;
    }
}
