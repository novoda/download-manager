package com.novoda.downloadmanager.demo.simple;

import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.FileSize;

import java.util.ArrayList;
import java.util.List;

class Migration {

    private Batch batch;
    private final List<String> originalFileLocations = new ArrayList<>();
    private final List<FileSize> fileSizes = new ArrayList<>();

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
