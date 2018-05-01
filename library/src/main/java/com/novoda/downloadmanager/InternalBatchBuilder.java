package com.novoda.downloadmanager;

interface InternalBatchBuilder extends BatchBuilder {
    void withFile(BatchFile batchFile);
}
