package com.novoda.downloadmanager;

import org.junit.Assert;
import org.junit.Test;

public class BatchStorageRootTest {

    private final StorageRoot storageRoot = () -> "path/to/the/root/downloads";
    private final DownloadBatchId downloadBatchId = () -> "batch_1";

    @Test
    public void appendsDownloadBatchIdToStorageRoot() {
        BatchStorageRoot batchStorageRoot = BatchStorageRoot.with(storageRoot, downloadBatchId);

        Assert.assertEquals(batchStorageRoot.path(), "path/to/the/root/downloads/batch_1");
    }
}