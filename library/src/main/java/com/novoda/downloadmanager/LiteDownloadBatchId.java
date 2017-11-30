package com.novoda.downloadmanager;

public class LiteDownloadBatchId implements DownloadBatchId {

    private final String id;

    public LiteDownloadBatchId(String id) {
        this.id = id;
    }

    @Override
    public String stringValue() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LiteDownloadBatchId)) {
            return false;
        }

        LiteDownloadBatchId that = (LiteDownloadBatchId) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "LiteDownloadBatchId{" +
                "id='" + id + '\'' +
                '}';
    }
}
