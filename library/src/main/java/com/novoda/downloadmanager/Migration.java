package com.novoda.downloadmanager;

import java.util.List;

class Migration {

    private final Batch batch;
    private List<OriginalMetadata> originalMetadata;

    Migration(Batch batch, List<OriginalMetadata> originalMetadata) {
       this.batch = batch;
        this.originalMetadata = originalMetadata;
    }

    Batch batch() {
        return batch;
    }

    List<OriginalMetadata> getOriginalMetadata() {
        return originalMetadata;
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
        return originalMetadata != null ? originalMetadata.equals(migration.originalMetadata) : migration.originalMetadata == null;
    }

    @Override
    public int hashCode() {
        int result = batch != null ? batch.hashCode() : 0;
        result = 31 * result + (originalMetadata != null ? originalMetadata.hashCode() : 0);
        return result;
    }

    static class OriginalMetadata {
        private final String originalFileLocation;
        private final FileSize fileSize;

        OriginalMetadata(String originalFileLocation, FileSize fileSize) {
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

            OriginalMetadata that = (OriginalMetadata) o;

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
