package com.novoda.downloadmanager;

public final class DownloadBatchIdCreator {

    private DownloadBatchIdCreator() {
        // non-instantiable class
    }

    /**
     * Sanitizes the given rawId replacing any of {@code [:\\\\/*?|<>]} with an underscore
     * before returning an instance of {@link DownloadBatchId}.
     *
     * @param rawId to be sanitized before creating {@link DownloadBatchId}.
     * @return an instance of {@link DownloadBatchId}.
     */
    public static DownloadBatchId createSanitizedFrom(String rawId) {
        String sanitizedBatchId = sanitizeBatchId(rawId);
        return new LiteDownloadBatchId(sanitizedBatchId);
    }

    private static String sanitizeBatchId(String batchIdPath) {
        return batchIdPath.replaceAll("[:\\\\/*?|<>]", "_");
    }
}
