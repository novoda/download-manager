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

    @Override
    public String toString() {
        return "DownloadId{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DownloadId that = (DownloadId) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
