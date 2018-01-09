package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

public final class Batch {

    private final DownloadBatchId downloadBatchId;
    private final String title;
    private final List<String> fileUrls;

    Batch(DownloadBatchId downloadBatchId, String title, List<String> fileUrls) {
        this.downloadBatchId = downloadBatchId;
        this.title = title;
        this.fileUrls = fileUrls;
    }

    public DownloadBatchId downloadBatchId() {
        return downloadBatchId;
    }

    public String title() {
        return title;
    }

    public List<String> fileUrls() {
        return new ArrayList<>(fileUrls);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Batch batch = (Batch) o;

        if (!downloadBatchId.equals(batch.downloadBatchId)) {
            return false;
        }
        if (!title.equals(batch.title)) {
            return false;
        }
        return fileUrls.equals(batch.fileUrls);
    }

    @Override
    public int hashCode() {
        int result = downloadBatchId.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + fileUrls.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Batch{"
                + "downloadBatchId=" + downloadBatchId
                + ", title='" + title + '\''
                + ", fileUrls=" + fileUrls
                + '}';
    }

    public static class Builder {

        private final DownloadBatchId downloadBatchId;
        private final String title;
        private final List<String> fileUrls = new ArrayList<>();

        public Builder(DownloadBatchId downloadBatchId, String title) {
            this.downloadBatchId = downloadBatchId;
            this.title = title;
        }

        public Builder addFile(String fileUrl) {
            fileUrls.add(fileUrl);
            return this;
        }

        public Batch build() {
            return new Batch(downloadBatchId, title, fileUrls);
        }
    }
}
