package com.novoda.downloadmanager.domain;

public class DownloadId {

    private final long id;

    public DownloadId(long id) {
        this.id = id;
    }

    public String asString() {
        return String.valueOf(id);
    }

    public long asLong() {
        return id;
    }
}
