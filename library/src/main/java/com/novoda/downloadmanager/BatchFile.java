package com.novoda.downloadmanager;

public class BatchFile {

    private final String networkAddress;
    private final String path;
    private final Optional<DownloadFileId> downloadFileId;

    BatchFile(String networkAddress, Optional<DownloadFileId> downloadFileId, String path) {
        this.networkAddress = networkAddress;
        this.downloadFileId = downloadFileId;
        this.path = path;
    }

    static InternalBatchFileBuilder from(StorageRoot storageRoot, DownloadBatchId downloadBatchId, String networkAddress) {
        return new LiteBatchFileBuilder(storageRoot, downloadBatchId, networkAddress);
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

}
