package com.novoda.downloadmanager;

import java.util.Collections;
import java.util.List;

public class Migration {

    public enum Type {
        COMPLETE,
        PARTIAL
    }

    private final Batch batch;
    private final List<FileMetadata> fileMetadata;
    private final long downloadedDateTimeInMillis;
    private final Type type;

    public Migration(Batch batch, List<FileMetadata> fileMetadata, long downloadedDateTimeInMillis, Type type) {
        this.batch = batch;
        this.fileMetadata = Collections.unmodifiableList(fileMetadata);
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        this.type = type;
    }

    public Batch batch() {
        return batch;
    }

    public List<FileMetadata> getFileMetadata() {
        return fileMetadata;
    }

    public long downloadedDateTimeInMillis() {
        return downloadedDateTimeInMillis;
    }

    public Type type() {
        return type;
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
        if (batch != null ? !batch.equals(migration.batch) : migration.batch != null) {
            return false;
        }
        if (fileMetadata != null ? !fileMetadata.equals(migration.fileMetadata) : migration.fileMetadata != null) {
            return false;
        }
        return type == migration.type;
    }

    @Override
    public int hashCode() {
        int result = batch != null ? batch.hashCode() : 0;
        result = 31 * result + (fileMetadata != null ? fileMetadata.hashCode() : 0);
        result = 31 * result + (int) (downloadedDateTimeInMillis ^ (downloadedDateTimeInMillis >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Migration{"
                + "batch=" + batch
                + ", fileMetadata=" + fileMetadata
                + ", downloadedDateTimeInMillis=" + downloadedDateTimeInMillis
                + ", type=" + type
                + '}';
    }

    public static class FileMetadata {

        private final String fileId;
        private final String originalFileLocation;
        private final String newFileLocation;
        private final long currentSizeInBytes;
        private final long totalSizeInBytes;
        private final String originalNetworkAddress;

        public FileMetadata(String fileId,
                            String originalFileLocation,
                            String newFileLocation,
                            long currentSizeInBytes, long totalSizeInBytes,
                            String originalNetworkAddress) {
            this.fileId = fileId;
            this.originalFileLocation = originalFileLocation;
            this.newFileLocation = newFileLocation;
            this.currentSizeInBytes = currentSizeInBytes;
            this.totalSizeInBytes = totalSizeInBytes;
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

        public long currentSizeInBytes() {
            return currentSizeInBytes;
        }

        public long totalSizeInBytes() {
            return totalSizeInBytes;
        }

        public String originalNetworkAddress() {
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

            if (currentSizeInBytes != that.currentSizeInBytes) {
                return false;
            }
            if (totalSizeInBytes != that.totalSizeInBytes) {
                return false;
            }
            if (fileId != null ? !fileId.equals(that.fileId) : that.fileId != null) {
                return false;
            }
            if (originalFileLocation != null ? !originalFileLocation.equals(that.originalFileLocation) : that.originalFileLocation != null) {
                return false;
            }
            if (newFileLocation != null ? !newFileLocation.equals(that.newFileLocation) : that.newFileLocation != null) {
                return false;
            }
            return originalNetworkAddress != null ? originalNetworkAddress.equals(that.originalNetworkAddress) : that.originalNetworkAddress == null;
        }

        @Override
        public int hashCode() {
            int result = fileId != null ? fileId.hashCode() : 0;
            result = 31 * result + (originalFileLocation != null ? originalFileLocation.hashCode() : 0);
            result = 31 * result + (newFileLocation != null ? newFileLocation.hashCode() : 0);
            result = 31 * result + (int) (currentSizeInBytes ^ (currentSizeInBytes >>> 32));
            result = 31 * result + (int) (totalSizeInBytes ^ (totalSizeInBytes >>> 32));
            result = 31 * result + (originalNetworkAddress != null ? originalNetworkAddress.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "FileMetadata{"
                    + "fileId='" + fileId + '\''
                    + ", originalFileLocation='" + originalFileLocation + '\''
                    + ", newFileLocation='" + newFileLocation + '\''
                    + ", currentSizeInBytes=" + currentSizeInBytes
                    + ", totalSizeInBytes=" + totalSizeInBytes
                    + ", originalNetworkAddress='" + originalNetworkAddress + '\''
                    + '}';
        }
    }
}
