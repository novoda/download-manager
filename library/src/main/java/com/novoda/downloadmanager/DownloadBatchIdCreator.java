package com.novoda.downloadmanager;

public final class DownloadBatchIdCreator {

    private DownloadBatchIdCreator() {
        // non-instantiable class
    }

    public static DownloadBatchId createFrom(String rawId) {
        return new LiteDownloadBatchId(rawId);
    }
}
