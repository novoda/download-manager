package com.novoda.downloadmanager.domain;

public class DownloadId {

    private final long id;

    public DownloadId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "" + id;
    }

    public long toLong() {
        return id;
    }
}
