package com.novoda.downloadmanager;

final class DownloadFileId {

    private final int id;

    static DownloadFileId from(Batch batch) {
        String id = batch.title() + System.nanoTime();
        return new DownloadFileId(id.hashCode());
    }

    static DownloadFileId from(String id) {
        return new DownloadFileId(Integer.parseInt(id));
    }

    private DownloadFileId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DownloadFileId)) {
            return false;
        }

        DownloadFileId that = (DownloadFileId) o;

        return id == that.id;

    }

    String toRawId() {
        return String.valueOf(id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "DownloadFileId{"
                + "id=" + id
                + '}';
    }
}
