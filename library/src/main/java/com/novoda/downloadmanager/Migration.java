package com.novoda.downloadmanager;

import java.util.Collections;
import java.util.List;

class Migration {

    enum Type {
        COMPLETE,
        PARTIAL
    }

    private final Batch batch;
    private final List<FileMetadata> fileMetadata;
    private final long downloadedDateTimeInMillis;
    private final Type type;

    Migration(Batch batch, List<FileMetadata> fileMetadata, long downloadedDateTimeInMillis, Type type) {
        this.batch = batch;
        this.fileMetadata = Collections.unmodifiableList(fileMetadata);
        this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        this.type = type;
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

    Type type() {
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
