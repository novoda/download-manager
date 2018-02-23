package com.novoda.downloadmanager;

import java.util.Collections;
import java.util.List;

class Migration {

    private final Batch batch;
    private final List<FileMetadata> fileMetadata;
    private final long downloadedDateTimeInMillis;

    Migration(Batch batch, List<FileMetadata> fileMetadata, long downloadedDateTimeInMillis) {
        this.batch = batch;
        this.fileMetadata = Collections.unmodifiableList(fileMetadata);
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
    }

    Batch batch() {
        return batch;
    }

    List<FileMetadata> getFileMetadata() {
        return fileMetadata;
    }

    public long downloadedDateTimeInMillis() {
        return downloadedDateTimeInMillis;
    }

    boolean hasDownloadedBatch() {
        for (FileMetadata fileMetadatum : fileMetadata) {
            if (fileMetadatum.fileSize().currentSize() != fileMetadatum.fileSize().totalSize()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Migration migration = (Migration) o;

        if (downloadedDateTimeInMillis != migration.downloadedDateTimeInMillis) {
            return false;
        }
        if (!batch.equals(migration.batch)) {
            return false;
        }
        return fileMetadata.equals(migration.fileMetadata);
    }

    @Override
    public int hashCode() {
        int result = batch.hashCode();
        result = 31 * result + fileMetadata.hashCode();
        result = 31 * result + (int) (downloadedDateTimeInMillis ^ (downloadedDateTimeInMillis >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Migration{"
                + "batch=" + batch
                + ", fileMetadata=" + fileMetadata
                + ", downloadedDateTimeInMillis=" + downloadedDateTimeInMillis
                + '}';
    }

    static class FileMetadata {

        private final String fileId;
        private final String originalFileLocation;
        private final FileSize fileSize;
        private final String originalNetworkAddress;

        FileMetadata(String fileId, String originalFileLocation, FileSize fileSize, String originalNetworkAddress) {
            this.fileId = fileId;
            this.originalFileLocation = originalFileLocation;
            this.fileSize = fileSize;
            this.originalNetworkAddress = originalNetworkAddress;
        }

        String fileId() {
            return fileId;
        }

        String originalFileLocation() {
            return originalFileLocation;
        }

        FileSize fileSize() {
            return fileSize;
        }

        String originalNetworkAddress() {
            return originalNetworkAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FileMetadata that = (FileMetadata) o;

            if (fileId != null ? !fileId.equals(that.fileId) : that.fileId != null) {
                return false;
            }
            if (originalFileLocation != null ? !originalFileLocation.equals(that.originalFileLocation) : that.originalFileLocation != null) {
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
            result = 31 * result + (fileSize != null ? fileSize.hashCode() : 0);
            result = 31 * result + (originalNetworkAddress != null ? originalNetworkAddress.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "FileMetadata{"
                    + "fileId='" + fileId + '\''
                    + ", originalFileLocation='" + originalFileLocation + '\''
                    + ", fileSize=" + fileSize
                    + ", originalNetworkAddress='" + originalNetworkAddress + '\''
                    + '}';
        }
    }
}
