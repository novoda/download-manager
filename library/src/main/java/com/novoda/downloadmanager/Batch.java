package com.novoda.downloadmanager;

import java.util.HashMap;
import java.util.Map;

public final class Batch {

    private final DownloadBatchId downloadBatchId;
    private final String title;
    private final Map<DownloadFileId, String> fileUrls;

    Batch(DownloadBatchId downloadBatchId, String title, Map<DownloadFileId, String> fileUrls) {
        this.downloadBatchId = downloadBatchId;
        this.title = title;
        this.fileUrls = fileUrls;
    }

    DownloadBatchId getDownloadBatchId() {
        return downloadBatchId;
    }

    String getTitle() {
        return title;
    }

    Map<DownloadFileId, String> getFileUrls() {
        return new HashMap<>(fileUrls);
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
        return "Batch{" +
                "downloadBatchId=" + downloadBatchId +
                ", title='" + title + '\'' +
                ", fileUrls=" + fileUrls +
                '}';
    }

    public static class Builder {

        private final DownloadBatchId downloadBatchId;
        private final String title;
        private final Map<DownloadFileId, String> fileUrls = new HashMap<>();

        public Builder(DownloadBatchId downloadBatchId, String title) {
            this.downloadBatchId = downloadBatchId;
            this.title = title;
        }

        public Builder addFile(String fileUrl) {
            String id = downloadBatchId.stringValue() + fileUrl;
            DownloadFileId downloadFileId = LiteDownloadFileId.from(String.valueOf(id.hashCode()));
            fileUrls.put(downloadFileId, fileUrl);
            return this;
        }

        public Builder addFile(DownloadFileId downloadFileId, String fileUrl) {
            fileUrls.put(downloadFileId, fileUrl);
            return this;
        }

        public Batch build() {
            return new Batch(downloadBatchId, title, fileUrls);
        }
    }
}
