package com.novoda.downloadmanager;

import java.io.File;

public class BatchFile {

    private final String networkAddress;
    private final String path;
    private final Optional<DownloadFileId> downloadFileId;

    BatchFile(String networkAddress, Optional<DownloadFileId> downloadFileId, String path) {
        this.networkAddress = networkAddress;
        this.downloadFileId = downloadFileId;
        this.path = path;
    }

    static InternalBuilder from(DownloadBatchId downloadBatchId, String networkAddress) {
        return new LiteFileBuilder(downloadBatchId, networkAddress);
    }

    public String networkAddress() {
        return networkAddress;
    }

    public String path() {
        return path;
    }

    public Optional<DownloadFileId> downloadFileId() {
        return downloadFileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BatchFile batchFile = (BatchFile) o;

        if (networkAddress != null ? !networkAddress.equals(batchFile.networkAddress) : batchFile.networkAddress != null) {
            return false;
        }
        if (path != null ? !path.equals(batchFile.path) : batchFile.path != null) {
            return false;
        }
        return downloadFileId != null ? downloadFileId.equals(batchFile.downloadFileId) : batchFile.downloadFileId == null;
    }

    @Override
    public int hashCode() {
        int result = networkAddress != null ? networkAddress.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (downloadFileId != null ? downloadFileId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BatchFile{"
                + "networkAddress='" + networkAddress + '\''
                + ", path='" + path + '\''
                + ", downloadFileId=" + downloadFileId
                + '}';
    }

    public interface Builder {
        Builder withIdentifier(DownloadFileId downloadFileId);

        Builder saveTo(String path);

        Builder saveTo(String path, String fileName);

        Batch.Builder apply();
    }

    interface InternalBuilder extends Builder {
        Builder withParentBuilder(Batch.InternalBuilder parentBuilder);
    }

    private static final class LiteFileBuilder implements InternalBuilder {

        private final DownloadBatchId downloadBatchId;
        private final String networkAddress;

        private Optional<DownloadFileId> downloadFileId = Optional.absent();
        private Optional<String> path = Optional.absent();

        private Batch.InternalBuilder parentBuilder;

        LiteFileBuilder(DownloadBatchId downloadBatchId, String networkAddress) {
            this.downloadBatchId = downloadBatchId;
            this.networkAddress = networkAddress;
        }

        @Override
        public Builder withParentBuilder(Batch.InternalBuilder parentBuilder) {
            this.parentBuilder = parentBuilder;
            return this;
        }

        @Override
        public Builder withIdentifier(DownloadFileId downloadFileId) {
            this.downloadFileId = Optional.fromNullable(downloadFileId);
            return this;
        }

        @Override
        public Builder saveTo(String path) {
            String networkAddressDerivedFileName = FileNameExtractor.extractFrom(networkAddress);
            return saveTo(path, networkAddressDerivedFileName);
        }

        @Override
        public Builder saveTo(String path, String fileName) {
            if (path != null && fileName != null) {
                this.path = Optional.of(path + fileName);
            }

            return this;
        }

        @Override
        public Batch.Builder apply() {
            String networkAddressDerivedFileName = FileNameExtractor.extractFrom(networkAddress);
            String pathPrependedWithBatchId = prependBatchIdTo(path.or(networkAddressDerivedFileName), downloadBatchId);

            parentBuilder.withFile(new BatchFile(networkAddress, downloadFileId, pathPrependedWithBatchId));
            return parentBuilder;
        }

        private static String prependBatchIdTo(String filePath, DownloadBatchId downloadBatchId) {
            return downloadBatchId.rawId() + File.separatorChar + filePath;
        }

    }
}
