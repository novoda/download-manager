package com.novoda.downloadmanager;

import java.util.List;

class Migration {

    private Batch batch;
    private final List<String> originalFileLocations;
    private final List<FileSize> fileSizes;

    Migration(Batch batch, List<String> originalFileLocations, List<FileSize> fileSizes) {
        this.batch = batch;
        this.originalFileLocations = originalFileLocations;
        this.fileSizes = fileSizes;
    }

    void add(String originalFileLocation, FileSize fileSize) {
        originalFileLocations.add(originalFileLocation);
        fileSizes.add(fileSize);
    }

    void setBatch(Batch batch) {
        this.batch = batch;
    }

    List<String> originalFileLocations() {
        return originalFileLocations;
    }

    List<FileSize> fileSizes() {
        return fileSizes;
    }

    Batch batch() {
        return batch;
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
        if (!originalFileLocations.equals(migration.originalFileLocations)) {
            return false;
        }
        return fileSizes.equals(migration.fileSizes);
    }

    @Override
    public int hashCode() {
        int result = batch.hashCode();
        result = 31 * result + originalFileLocations.hashCode();
        result = 31 * result + fileSizes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Migration{" +
                "batch=" + batch +
                ", originalFileLocations=" + originalFileLocations +
                ", fileSizes=" + fileSizes +
                '}';
    }
}
