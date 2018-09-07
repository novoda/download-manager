package com.novoda.downloadmanager.demo.migration;

import com.novoda.downloadmanager.Batch;

import java.util.List;

public class VersionOnePartialDownloadBatch {

    private final Batch batch;
    private final List<String> originalFileLocations;

    VersionOnePartialDownloadBatch(Batch batch, List<String> originalFileLocations) {
        this.batch = batch;
        this.originalFileLocations = originalFileLocations;
    }

    public Batch batch() {
        return batch;
    }

    public List<String> originalFileLocations() {
        return originalFileLocations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VersionOnePartialDownloadBatch that = (VersionOnePartialDownloadBatch) o;

        if (batch != null ? !batch.equals(that.batch) : that.batch != null) {
            return false;
        }
        return originalFileLocations != null ? originalFileLocations.equals(that.originalFileLocations) : that.originalFileLocations == null;
    }

    @Override
    public int hashCode() {
        int result = batch != null ? batch.hashCode() : 0;
        result = 31 * result + (originalFileLocations != null ? originalFileLocations.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VersionOnePartialDownloadBatch{"
                + "batch=" + batch
                + ", originalFileLocations=" + originalFileLocations
                + '}';
    }
}
