package com.novoda.downloadmanager;

/**
 * Represents a unique identifier for a download batch.
 * Clients should use {@link DownloadBatchIdCreator} and pass to {@link Batch#with(StorageRoot, DownloadBatchId, String)}
 * when creating batches of downloads.
 */
public interface DownloadBatchId {

    String rawId();
}
