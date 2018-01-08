package com.novoda.downloadmanager;

final class LiteDownloadFileId implements DownloadFileId {

    private final int id;

    LiteDownloadFileId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LiteDownloadFileId)) {
            return false;
        }

        LiteDownloadFileId that = (LiteDownloadFileId) o;

        return id == that.id;

    }

    @Override
    public String toRawId() {
        return String.valueOf(id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "LiteDownloadFileId{" +
                "id=" + id +
                '}';
    }
}
