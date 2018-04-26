package com.novoda.downloadmanager;

public class BatchFile {

    private final String networkAddress;
    private final Optional<DownloadFileId> downloadFileId;
    private final Optional<String> path;

    BatchFile(String networkAddress, Optional<DownloadFileId> downloadFileId, Optional<String> path) {
        this.networkAddress = networkAddress;
        this.downloadFileId = downloadFileId;
        this.path = path;
    }

    static InternalBuilder downloadFrom(String networkAddress) {
        return new LiteFileBuilder(networkAddress);
    }

    public String networkAddress() {
        return networkAddress;
    }

    public Optional<DownloadFileId> downloadFileId() {
        return downloadFileId;
    }

    public Optional<String> path() {
        return path;
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
        return path != null ? path.equals(batchFile.path) : batchFile.path == null;
    }

    @Override
    public int hashCode() {
        int result = networkAddress != null ? networkAddress.hashCode() : 0;
        result = 31 * result + (downloadFileId != null ? downloadFileId.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BatchFile{"
                + "networkAddress='" + networkAddress + '\''
                + ", downloadFileId=" + downloadFileId
                + ", path=" + path
                + '}';
    }

    public interface Builder {
        Builder withIdentifier(DownloadFileId downloadFileId);

        Builder saveTo(String path);

        Batch.Builder apply();
    }

    interface InternalBuilder extends Builder {
        Builder withParentBuilder(Batch.InternalBuilder parentBuilder);
    }

    private static final class LiteFileBuilder implements InternalBuilder {

        private final String networkAddress;
        private Optional<DownloadFileId> downloadFileId = Optional.absent();
        private Optional<String> path = Optional.absent();

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
        public Builder withIdentifier(DownloadFileId downloadFileId) {
            this.downloadFileId = Optional.fromNullable(downloadFileId);
            return this;
        }

        @Override
        public Builder saveTo(String path) {
            this.path = Optional.fromNullable(path);
            return this;
        }

        @Override
        public Batch.Builder apply() {
            parentBuilder.withFile(new BatchFile(networkAddress, downloadFileId, path));
            return parentBuilder;
        }

    }
}
