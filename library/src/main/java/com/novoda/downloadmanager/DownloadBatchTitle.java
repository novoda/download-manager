package com.novoda.downloadmanager;

/**
 * Represents a title for a download batch.
 * Clients can specify this whenever creating a batch through {@link Batch#with(StorageRoot, DownloadBatchId, String)}.
 */
public interface DownloadBatchTitle {

    String asString();
}
