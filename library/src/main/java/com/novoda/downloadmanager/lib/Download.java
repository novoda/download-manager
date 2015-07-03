package com.novoda.downloadmanager.lib;

public class Download {

    private final long id;
    private final long currentSize;
    private final long totalSize;

    public Download(long id, long currentSize, long totalSize) {
        this.id = id;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
    }

    public long getId() {
        return id;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
