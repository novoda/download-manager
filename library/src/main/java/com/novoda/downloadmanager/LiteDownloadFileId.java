package com.novoda.downloadmanager;

final class LiteDownloadFileId implements DownloadFileId {

    private final String id;

    LiteDownloadFileId(String id) {
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

        LiteDownloadFileId that = (LiteDownloadFileId) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LiteDownloadFileId{"
                + "id='" + id + '\''
                + '}';
    }
}
