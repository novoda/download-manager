package com.novoda.downloadmanager;

import java.util.List;

public class CompletedDownloadBatch {

    private final DownloadBatchId downloadBatchId;
    private final DownloadBatchTitle downloadBatchTitle;
    private final long downloadedDateTimeInMillis;
    private final List<CompletedDownloadFile> completedDownloadFiles;

    public CompletedDownloadBatch(DownloadBatchId downloadBatchId, DownloadBatchTitle downloadBatchTitle, long downloadedDateTimeInMillis, List<CompletedDownloadFile> completedDownloadFiles) {
        this.downloadBatchId = downloadBatchId;
        this.downloadBatchTitle = downloadBatchTitle;
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        this.completedDownloadFiles = completedDownloadFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompletedDownloadBatch that = (CompletedDownloadBatch) o;

        if (downloadedDateTimeInMillis != that.downloadedDateTimeInMillis) {
            return false;
        }
        if (downloadBatchId != null ? !downloadBatchId.equals(that.downloadBatchId) : that.downloadBatchId != null) {
            return false;
        }
        if (downloadBatchTitle != null ? !downloadBatchTitle.equals(that.downloadBatchTitle) : that.downloadBatchTitle != null) {
            return false;
        }
        return completedDownloadFiles != null ? completedDownloadFiles.equals(that.completedDownloadFiles) : that.completedDownloadFiles == null;
    }

    @Override
    public int hashCode() {
        int result = downloadBatchId != null ? downloadBatchId.hashCode() : 0;
        result = 31 * result + (downloadBatchTitle != null ? downloadBatchTitle.hashCode() : 0);
        result = 31 * result + (int) (downloadedDateTimeInMillis ^ (downloadedDateTimeInMillis >>> 32));
        result = 31 * result + (completedDownloadFiles != null ? completedDownloadFiles.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompletedDownloadBatch{"
                + "downloadBatchId=" + downloadBatchId
                + ", downloadBatchTitle=" + downloadBatchTitle
                + ", downloadedDateTimeInMillis=" + downloadedDateTimeInMillis
                + ", completedDownloadFiles=" + completedDownloadFiles
                + '}';
    }

    public static class CompletedDownloadFile {

        private final String fileId;
        private final String originalFileLocation;
        private final String newFileLocation;
        private final String fileSize;
        private final String originalNetworkAddress;

        public CompletedDownloadFile(String fileId, String originalFileLocation, String newFileLocation, String fileSize, String originalNetworkAddress) {
            this.fileId = fileId;
            this.originalFileLocation = originalFileLocation;
            this.newFileLocation = newFileLocation;
            this.fileSize = fileSize;
            this.originalNetworkAddress = originalNetworkAddress;
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
                    + ", fileSize='" + fileSize + '\''
                    + ", originalNetworkAddress='" + originalNetworkAddress + '\''
                    + '}';
        }
    }
}
