package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

public final class Batch {

    private final DownloadBatchId downloadBatchId;
    private final String title;
    private final List<String> fileUrls;

    private Batch(DownloadBatchId downloadBatchId, String title, List<String> fileUrls) {
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

    List<String> getFileUrls() {
        return new ArrayList<>(fileUrls);
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
