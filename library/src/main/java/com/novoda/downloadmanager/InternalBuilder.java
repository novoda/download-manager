package com.novoda.downloadmanager;

interface InternalBuilder extends Builder {
    Builder withParentBuilder(Batch.InternalBuilder parentBuilder);
}
