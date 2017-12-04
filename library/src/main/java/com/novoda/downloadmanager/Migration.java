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
}
