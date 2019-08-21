package com.novoda.downloadmanager;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class BatchStorageRootTest {

    private final StorageRoot storageRoot = () -> "path/to/the/root/downloads";
    private final DownloadBatchId downloadBatchId = () -> "batch_1";

    @Test
    public void appendsDownloadBatchIdToStorageRoot() {
        BatchStorageRoot batchStorageRoot = BatchStorageRoot.with(storageRoot, downloadBatchId);

        assertThat(batchStorageRoot.path()).isEqualTo("path/to/the/root/downloads/batch_1");
    }
}