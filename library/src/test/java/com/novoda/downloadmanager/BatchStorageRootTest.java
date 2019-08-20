package com.novoda.downloadmanager;

import org.junit.Assert;
import org.junit.Test;

public class BatchStorageRootTest {

    private final StorageRoot storageRoot = () -> "path/to/the/root/downloads";
    private final DownloadBatchId downloadBatchId = () -> "batch_1";

    @Test
    public void path() {
        BatchStorageRoot batchStorageRoot = BatchStorageRoot.with(storageRoot, downloadBatchId);
        String expected = "path/to/the/root/downloads/batch_1";
        Assert.assertEquals(batchStorageRoot.path(), expected);
    }
}