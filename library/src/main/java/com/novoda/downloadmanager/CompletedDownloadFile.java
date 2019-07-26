package com.novoda.downloadmanager;

public class CompletedDownloadFile {

    private final String fileId;
    private final String originalFileLocation;
    private final String newFileLocation;
    private final FileSize fileSize;
    private final String originalNetworkAddress;

    public CompletedDownloadFile(String fileId,
                                 String originalFileLocation,
                                 String newFileLocation,
                                 FileSize fileSize,
                                 String originalNetworkAddress) {
        this.fileId = fileId;
        this.originalFileLocation = originalFileLocation;
        this.newFileLocation = newFileLocation;
        this.fileSize = fileSize;
        this.originalNetworkAddress = originalNetworkAddress;
    }

    public String fileId() {
        return fileId;
    }

    public String originalFileLocation() {
        return originalFileLocation;
    }

    public String newFileLocation() {
        return newFileLocation;
    }

    public FileSize fileSize() {
        return fileSize;
    }

    public String originalNetworkAddress() {
        return originalNetworkAddress;
    }

    public BatchFile asBatchFile() {
        DownloadFileId downloadFileId = DownloadFileIdCreator.createFrom(fileId);
        return new BatchFile(
                originalNetworkAddress,
                newFileLocation,
                Optional.of(downloadFileId),
                Optional.of(fileSize)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompletedDownloadFile that = (CompletedDownloadFile) o;

        if (fileId != null ? !fileId.equals(that.fileId) : that.fileId != null) {
            return false;
        }
        if (originalFileLocation != null ? !originalFileLocation.equals(that.originalFileLocation) : that.originalFileLocation != null) {
            return false;
        }
        if (newFileLocation != null ? !newFileLocation.equals(that.newFileLocation) : that.newFileLocation != null) {
            return false;
        }
        if (fileSize != null ? !fileSize.equals(that.fileSize) : that.fileSize != null) {
            return false;
        }
        return originalNetworkAddress != null ? originalNetworkAddress.equals(that.originalNetworkAddress) : that.originalNetworkAddress == null;
    }

    @Override
    public int hashCode() {
        int result = fileId != null ? fileId.hashCode() : 0;
        result = 31 * result + (originalFileLocation != null ? originalFileLocation.hashCode() : 0);
        result = 31 * result + (newFileLocation != null ? newFileLocation.hashCode() : 0);
        result = 31 * result + (fileSize != null ? fileSize.hashCode() : 0);
        result = 31 * result + (originalNetworkAddress != null ? originalNetworkAddress.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompletedDownloadFile{"
                + "fileId='" + fileId + '\''
                + ", originalFileLocation='" + originalFileLocation + '\''
                + ", newFileLocation='" + newFileLocation + '\''
                + ", fileSize=" + fileSize
                + ", originalNetworkAddress='" + originalNetworkAddress + '\''
                + '}';
    }
}
