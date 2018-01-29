package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

public class Batch {

    private final DownloadBatchId downloadBatchId;
    private final String title;
    private final List<File> files;

    public static Builder with(DownloadBatchId downloadBatchId, String title) {
        return new LiteBatchBuilder(downloadBatchId, title, new ArrayList<>());
    }

    Batch(DownloadBatchId downloadBatchId, String title, List<File> files) {
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

        Batch batch = (Batch) o;

        if (downloadBatchId != null ? !downloadBatchId.equals(batch.downloadBatchId) : batch.downloadBatchId != null) {
            return false;
        }
        if (title != null ? !title.equals(batch.title) : batch.title != null) {
            return false;
        }
        return files != null ? files.equals(batch.files) : batch.files == null;
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
        return "Batch{"
                + "downloadBatchId=" + downloadBatchId
                + ", title='" + title + '\''
                + ", files=" + files
                + '}';
    }

    public interface Builder {
        File.Builder addFile(String networkAddress);

        Batch build();
    }

    interface InternalBuilder extends Builder {
        void withFile(File file);
    }

    private static final class LiteBatchBuilder implements InternalBuilder {

        private final DownloadBatchId downloadBatchId;
        private final String title;
        private final List<File> files;

        LiteBatchBuilder(DownloadBatchId downloadBatchId, String title, List<File> files) {
            this.downloadBatchId = downloadBatchId;
            this.title = title;
            this.files = files;
        }

        @Override
        public void withFile(File file) {
            files.add(file);
        }

        private File.Builder fileBuilder;

        @Override
        public File.Builder addFile(String networkAddress) {
            this.fileBuilder = File.with(networkAddress).withParentBuilder(this);
            return this.fileBuilder;
        }

        @Override
        public Batch build() {
            return new Batch(downloadBatchId, title, files);
        }

    }

}
