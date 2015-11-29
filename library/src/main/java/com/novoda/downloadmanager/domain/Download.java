package com.novoda.downloadmanager.domain;

import java.util.List;

public class Download {

    private final DownloadId id;
    private final long currentSize;
    private final long totalSize;
    private final DownloadStage downloadStage;
    private final DownloadStatus downloadStatus;
    private final List<DownloadFile> files;

    public Download(DownloadId id, long currentSize, long totalSize, DownloadStage downloadStage, DownloadStatus downloadStatus, List<DownloadFile> files) {
        this.id = id;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
        this.downloadStage = downloadStage;
        this.downloadStatus = downloadStatus;
        this.files = files;
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
        return (int) (((float) currentSize / (float) totalSize) * 100);
    }

}
