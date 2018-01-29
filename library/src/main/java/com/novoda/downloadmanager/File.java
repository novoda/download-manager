package com.novoda.downloadmanager;

public class File {

    private final String networkAddress;
    private final Optional<DownloadFileId> downloadFileId;
    private final Optional<FileName> fileName;
    private final Optional<String> relativePath;

    File(String networkAddress, Optional<DownloadFileId> downloadFileId, Optional<FileName> fileName, Optional<String> relativePath) {
        this.networkAddress = networkAddress;
        this.downloadFileId = downloadFileId;
        this.fileName = fileName;
        this.relativePath = relativePath;
    }

    static Builder newBuilder(String networkAddress) {
        return new Builder(networkAddress);
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

        File file = (File) o;

        if (networkAddress != null ? !networkAddress.equals(file.networkAddress) : file.networkAddress != null) {
            return false;
        }
        if (downloadFileId != null ? !downloadFileId.equals(file.downloadFileId) : file.downloadFileId != null) {
            return false;
        }
        if (fileName != null ? !fileName.equals(file.fileName) : file.fileName != null) {
            return false;
        }
        return relativePath != null ? relativePath.equals(file.relativePath) : file.relativePath == null;
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
        return "File{"
                + "networkAddress='" + networkAddress + '\''
                + ", downloadFileId=" + downloadFileId
                + ", fileName=" + fileName
                + ", relativePath=" + relativePath
                + '}';
    }

    public static class Builder {

        private final String networkAddress;
        private Optional<DownloadFileId> downloadFileId = Optional.absent();
        private Optional<FileName> fileName = Optional.absent();
        private Optional<String> relativePath = Optional.absent();

        private Batch.InternalBatchBuilder parentBuilder;

        public Builder(String networkAddress) {
            this.networkAddress = networkAddress;
        }

        Builder withParentBuilder(Batch.InternalBatchBuilder parentBuilder) {
            this.parentBuilder = parentBuilder;
            return this;
        }

        public Builder withDownloadFileId(DownloadFileId downloadFileId) {
            this.downloadFileId = Optional.of(downloadFileId);
            return this;
        }

        public Builder withFileName(FileName fileName) {
            this.fileName = Optional.of(fileName);
            return this;
        }

        public Builder withRelativePath(String relativePath) {
            this.relativePath = Optional.of(relativePath);
            return this;
        }

        public Batch.Builder apply() {
            parentBuilder.withFile(new File(networkAddress, downloadFileId, fileName, relativePath));
            return parentBuilder;
        }

    }
}
