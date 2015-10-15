package com.novoda.downloadmanager;

public class Download {

    private final long id;
    private final String title;
    private final String description;
    private final long currentSize;
    private final long totalSize;

    public Download(long id, String title, String description, long currentSize, long totalSize) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
