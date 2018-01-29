package com.novoda.downloadmanager;

public class BatchFile {

    private final String networkAddress;
    private final Optional<DownloadFileId> downloadFileId;
    private final Optional<FileName> fileName;
    private final Optional<String> relativePath;

    BatchFile(String networkAddress, Optional<DownloadFileId> downloadFileId, Optional<FileName> fileName, Optional<String> relativePath) {
        this.networkAddress = networkAddress;
        this.downloadFileId = downloadFileId;
        this.fileName = fileName;
        this.relativePath = relativePath;
    }

    static InternalBuilder with(String networkAddress) {
        return new LiteFileBuilder(networkAddress);
    }

    public String networkAddress() {
        return networkAddress;
    }

    public Optional<DownloadFileId> downloadFileId() {
        return downloadFileId;
    }

    public Optional<FileName> fileName() {
        return fileName;
    }

    public Optional<String> relativePath() {
        return relativePath;
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
        if (downloadFileId != null ? !downloadFileId.equals(batchFile.downloadFileId) : batchFile.downloadFileId != null) {
            return false;
        }
        if (fileName != null ? !fileName.equals(batchFile.fileName) : batchFile.fileName != null) {
            return false;
        }
        return relativePath != null ? relativePath.equals(batchFile.relativePath) : batchFile.relativePath == null;
    }

    @Override
    public int hashCode() {
        int result = networkAddress != null ? networkAddress.hashCode() : 0;
        result = 31 * result + (downloadFileId != null ? downloadFileId.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (relativePath != null ? relativePath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BatchFile{"
                + "networkAddress='" + networkAddress + '\''
                + ", downloadFileId=" + downloadFileId
                + ", fileName=" + fileName
                + ", relativePath=" + relativePath
                + '}';
    }

    public interface Builder {
        Builder withDownloadFileId(DownloadFileId downloadFileId);

        Builder withFileName(FileName fileName);

        Builder withRelativePath(String relativePath);

        Batch.Builder apply();
    }

    interface InternalBuilder extends Builder {
        Builder withParentBuilder(Batch.InternalBuilder parentBuilder);
    }

    private static final class LiteFileBuilder implements InternalBuilder {

        private final String networkAddress;
        private Optional<DownloadFileId> downloadFileId = Optional.absent();
        private Optional<FileName> fileName = Optional.absent();
        private Optional<String> relativePath = Optional.absent();

        private Batch.InternalBuilder parentBuilder;

        LiteFileBuilder(String networkAddress) {
            this.networkAddress = networkAddress;
        }

        @Override
        public Builder withParentBuilder(Batch.InternalBuilder parentBuilder) {
            this.parentBuilder = parentBuilder;
            return this;
        }

        @Override
        public Builder withDownloadFileId(DownloadFileId downloadFileId) {
            this.downloadFileId = Optional.fromNullable(downloadFileId);
            return this;
        }

        @Override
        public Builder withFileName(FileName fileName) {
            this.fileName = Optional.fromNullable(fileName);
            return this;
        }

        @Override
        public Builder withRelativePath(String relativePath) {
            this.relativePath = Optional.fromNullable(relativePath);
            return this;
        }

        @Override
        public Batch.Builder apply() {
            parentBuilder.withFile(new BatchFile(networkAddress, downloadFileId, fileName, relativePath));
            return parentBuilder;
        }

    }
}
