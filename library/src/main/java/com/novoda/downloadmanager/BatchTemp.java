package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

public class BatchTemp {

    private final DownloadBatchId downloadBatchId;
    private final String title;
    private final List<File> files;

    BatchTemp(DownloadBatchId downloadBatchId, String title, List<File> files) {

        this.downloadBatchId = downloadBatchId;
        this.title = title;
        this.files = files;
    }

    public DownloadBatchId downloadBatchId() {
        return downloadBatchId;
    }

    public String title() {
        return title;
    }

    public List<File> files() {
        return files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BatchTemp batchTemp = (BatchTemp) o;

        if (downloadBatchId != null ? !downloadBatchId.equals(batchTemp.downloadBatchId) : batchTemp.downloadBatchId != null) {
            return false;
        }
        if (title != null ? !title.equals(batchTemp.title) : batchTemp.title != null) {
            return false;
        }
        return files != null ? files.equals(batchTemp.files) : batchTemp.files == null;
    }

    @Override
    public int hashCode() {
        int result = downloadBatchId != null ? downloadBatchId.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (files != null ? files.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BatchTemp{"
                + "downloadBatchId=" + downloadBatchId
                + ", title='" + title + '\''
                + ", files=" + files
                + '}';
    }

    public static class Builder {

        private final DownloadBatchId downloadBatchId;
        private final String title;
        private List<File> files;

        public Builder(DownloadBatchId downloadBatchId, String title) {
            this.downloadBatchId = downloadBatchId;
            this.title = title;
            files = new ArrayList<>();
        }

        Builder withFile(File file) {
            files.add(file);
            return this;
        }

        private File.Builder fileBuilder;

        public File.Builder addFile(String networkAddress) {
            this.fileBuilder = File.newBuilder(networkAddress).withParentBuilder(this);
            return this.fileBuilder;
        }

        public BatchTemp build() {
            return new BatchTemp(downloadBatchId, title, files);
        }

    }

}
