package com.novoda.downloadmanager;

/**
 * Represents the shared storage root for a batch or batches.
 * <p>
 * The shared storage root for a downloaded file with storage location
 * `/data/user/0/com.novoda.downloadmanager.demo.simple/files/downloads/batch_id_2/20MB.zip`
 * would be `/data/user/0/com.novoda.downloadmanager.demo.simple/files/downloads`.
 */
public interface StorageRoot {

    String path();

}
