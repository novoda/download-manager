package com.novoda.downloadmanager;

class LiteDownloadBatchId implements DownloadBatchId {

    private final String id;

    LiteDownloadBatchId(String id) {
        this.id = id;
    }

    @Override
    public String rawId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LiteDownloadBatchId that = (LiteDownloadBatchId) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LiteDownloadBatchId{"
                + "id='" + id + '\''
                + '}';
    }
}
