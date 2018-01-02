package com.novoda.downloadmanager;

class LiteDownloadBatchTitle implements DownloadBatchTitle {

    private final String title;

    LiteDownloadBatchTitle(String title) {
        this.title = title;
    }

    @Override
    public String asString() {
        return title;
    }

    @Override
    public String toString() {
        return "LiteDownloadBatchTitle{"
                + "title='" + title + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LiteDownloadBatchTitle that = (LiteDownloadBatchTitle) o;

        return title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }
}
