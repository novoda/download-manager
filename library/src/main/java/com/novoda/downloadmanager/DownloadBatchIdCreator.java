package com.novoda.downloadmanager;

public final class DownloadBatchIdCreator {

    private DownloadBatchIdCreator() {
        // non-instantiable class
    }

    public static DownloadBatchId createSanitizedFrom(String rawId) {
        String sanitizedBatchId = sanitizeBatchId(rawId);
        return new LiteDownloadBatchId(sanitizedBatchId);
    }

    private static String sanitizeBatchId(String batchIdPath) {
        return batchIdPath.replaceAll("[:\\\\/*?|<>]", "_");
    }
}
