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

        if (!batch.equals(migration.batch)) {
            return false;
        }
        return fileMetadata.equals(migration.fileMetadata);
    }

    @Override
    public int hashCode() {
        int result = batch.hashCode();
        result = 31 * result + fileMetadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Migration{"
                + "batch=" + batch
                + ", fileMetadata=" + fileMetadata
                + '}';
    }

    static class FileMetadata {

        private final String originalFileLocation;
        private final FileSize fileSize;
        private final String uri;
        private final long downloadedDateTimeInMillis;

        FileMetadata(String originalFileLocation, FileSize fileSize, String uri, long downloadedDateTimeInMillis) {
            this.originalFileLocation = originalFileLocation;
            this.fileSize = fileSize;
            this.uri = uri;
            this.downloadedDateTimeInMillis = downloadedDateTimeInMillis;
        }

        String originalFileLocation() {
            return originalFileLocation;
        }

        FileSize fileSize() {
            return fileSize;
        }

        String uri() {
            return uri;
        }

        long downloadedDateTimeInMillis() {
            return downloadedDateTimeInMillis;
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

            if (downloadedDateTimeInMillis != that.downloadedDateTimeInMillis) {
                return false;
            }
            if (!originalFileLocation.equals(that.originalFileLocation)) {
                return false;
            }
            if (!fileSize.equals(that.fileSize)) {
                return false;
            }
            return uri.equals(that.uri);
        }

        @Override
        public int hashCode() {
            int result = originalFileLocation.hashCode();
            result = 31 * result + fileSize.hashCode();
            result = 31 * result + uri.hashCode();
            result = 31 * result + (int) (downloadedDateTimeInMillis ^ (downloadedDateTimeInMillis >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "FileMetadata{"
                    + "originalFileLocation='" + originalFileLocation + '\''
                    + ", fileSize=" + fileSize
                    + ", uri='" + uri + '\''
                    + ", downloadedDateTimeInMillis=" + downloadedDateTimeInMillis
                    + '}';
        }
    }
}
