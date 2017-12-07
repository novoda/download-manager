package com.novoda.downloadmanager;

import java.util.List;

class Migration {

    private final Batch batch;
    private List<FileMetadata> fileMetadata;

    Migration(Batch batch, List<FileMetadata> fileMetadata) {
       this.batch = batch;
        this.fileMetadata = fileMetadata;
    }

    Batch batch() {
        return batch;
    }

    List<FileMetadata> getFileMetadata() {
        return fileMetadata;
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

        FileMetadata(String originalFileLocation, FileSize fileSize) {
            this.originalFileLocation = originalFileLocation;
            this.fileSize = fileSize;
        }

        String getOriginalFileLocation() {
            return originalFileLocation;
        }

        FileSize getFileSize() {
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

            FileMetadata that = (FileMetadata) o;

            if (originalFileLocation != null ? !originalFileLocation.equals(that.originalFileLocation) : that.originalFileLocation != null) {
                return false;
            }
            return fileSize != null ? fileSize.equals(that.fileSize) : that.fileSize == null;
        }

        @Override
        public int hashCode() {
            int result = originalFileLocation != null ? originalFileLocation.hashCode() : 0;
            result = 31 * result + (fileSize != null ? fileSize.hashCode() : 0);
            return result;
        }
    }
}
