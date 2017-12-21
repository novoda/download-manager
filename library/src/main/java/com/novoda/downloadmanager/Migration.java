package com.novoda.downloadmanager;

import java.util.Collections;
import java.util.List;

class Migration {

    private final Batch batch;
    private final List<FileMetadata> fileMetadata;

    Migration(Batch batch, List<FileMetadata> fileMetadata) {
        this.batch = batch;
        this.fileMetadata = Collections.unmodifiableList(fileMetadata);
    }

    Batch batch() {
        return batch;
    }

    List<FileMetadata> getFileMetadata() {
        return fileMetadata;
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

        if (batch != null ? !batch.equals(migration.batch) : migration.batch != null) {
            return false;
        }
        return fileMetadata != null ? fileMetadata.equals(migration.fileMetadata) : migration.fileMetadata == null;
    }

    @Override
    public int hashCode() {
        int result = batch != null ? batch.hashCode() : 0;
        result = 31 * result + (fileMetadata != null ? fileMetadata.hashCode() : 0);
        return result;
    }

    static class FileMetadata {
        private final String originalFileLocation;
        private final FileSize fileSize;
        private final String uri;

        FileMetadata(String originalFileLocation, FileSize fileSize, String uri) {
            this.originalFileLocation = originalFileLocation;
            this.fileSize = fileSize;
            this.uri = uri;
        }

        String originalFileLocation() {
            return originalFileLocation;
        }

        FileSize fileSize() {
            return fileSize;
        }

        public String uri() {
            return uri;
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

            if (originalFileLocation != null ? !originalFileLocation.equals(that.originalFileLocation) : that.originalFileLocation != null) {
                return false;
            }
            if (fileSize != null ? !fileSize.equals(that.fileSize) : that.fileSize != null) {
                return false;
            }
            return uri != null ? uri.equals(that.uri) : that.uri == null;
        }

        @Override
        public int hashCode() {
            int result = originalFileLocation != null ? originalFileLocation.hashCode() : 0;
            result = 31 * result + (fileSize != null ? fileSize.hashCode() : 0);
            result = 31 * result + (uri != null ? uri.hashCode() : 0);
            return result;
        }
    }
}
