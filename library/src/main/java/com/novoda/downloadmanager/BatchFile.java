package com.novoda.downloadmanager;

public class BatchFile {

    private final String networkAddress;
    private final String path;
    private final Optional<DownloadFileId> downloadFileId;
    private final Optional<FileSize> fileSize;

    public BatchFile(String networkAddress, String path, Optional<DownloadFileId> downloadFileId, Optional<FileSize> fileSize) {
        this.networkAddress = networkAddress;
        this.path = path;
        this.downloadFileId = downloadFileId;
        this.fileSize = fileSize;
    }

    static InternalBatchFileBuilder from(DownloadBatchStorageRoot batchStorageRoot, String networkAddress) {
        return new LiteBatchFileBuilder(batchStorageRoot, networkAddress);
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

    public Optional<FileSize> fileSize() {
        return fileSize;
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
        if (downloadFileId != null ? !downloadFileId.equals(batchFile.downloadFileId) : batchFile.downloadFileId != null) {
            return false;
        }
        return fileSize != null ? fileSize.equals(batchFile.fileSize) : batchFile.fileSize == null;
    }

    @Override
    public int hashCode() {
        int result = networkAddress != null ? networkAddress.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (downloadFileId != null ? downloadFileId.hashCode() : 0);
        result = 31 * result + (fileSize != null ? fileSize.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BatchFile{"
                + "networkAddress='" + networkAddress + '\''
                + ", path='" + path + '\''
                + ", downloadFileId=" + downloadFileId
                + ", fileSize=" + fileSize
                + '}';
    }
}
