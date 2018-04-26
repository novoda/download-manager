package com.novoda.downloadmanager;

public interface BatchBuilder {
    BatchFileBuilder downloadFrom(String networkAddress);

    Batch build();
}
