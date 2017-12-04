package com.novoda.downloadmanager.demo.simple;

import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.FileSize;

import java.util.List;

class Migration {

    private Batch batch;
    private final List<String> originalFileLocations;
    private final List<FileSize> fileSizes;

    public Migration(Batch batch, List<String> originalFileLocations, List<FileSize> fileSizes) {
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
