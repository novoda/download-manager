package com.novoda.downloadmanager;

import java.util.List;

public class Download {

    private final DownloadId id;
    private final long currentSize;
    private final long totalSize;
    private final DownloadStage downloadStage;
    private final DownloadStatus downloadStatus;
    private final List<DownloadFile> files;
    private final ExternalId externalId;

    public Download(DownloadId id, long currentSize, long totalSize, DownloadStage downloadStage, DownloadStatus downloadStatus, List<DownloadFile> files, ExternalId externalId) {
        this.id = id;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
        this.downloadStage = downloadStage;
        this.downloadStatus = downloadStatus;
        this.files = files;
        this.externalId = externalId;
    }

    public DownloadId getId() {
        return id;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public List<DownloadFile> getFiles() {
        return files;
    }

    public DownloadStage getStage() {
        return downloadStage;
    }

    public DownloadStatus getStatus() {
        return downloadStatus;
    }

    public int getPercentage() {
        return Percentage.of(currentSize, totalSize);
    }

    public ExternalId externalId() {
        return externalId;
    }

}
