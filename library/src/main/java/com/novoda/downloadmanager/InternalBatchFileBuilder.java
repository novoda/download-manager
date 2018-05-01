package com.novoda.downloadmanager;

interface InternalBatchFileBuilder extends BatchFileBuilder {
    BatchFileBuilder withParentBuilder(InternalBatchBuilder parentBuilder);
}
