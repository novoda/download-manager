package com.novoda.downloadmanager.domain;

import java.util.List;

public class Download {

    private final DownloadId id;
    private final long currentSize;
    private final long totalSize;
    private final DownloadStatus downloadStatus;
    private final List<DownloadFile> files;

    public Download(DownloadId id, long currentSize, long totalSize, DownloadStatus downloadStatus, List<DownloadFile> files) {
        this.id = id;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
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

    public DownloadStatus getStatus() {
        return downloadStatus;
    }

}
