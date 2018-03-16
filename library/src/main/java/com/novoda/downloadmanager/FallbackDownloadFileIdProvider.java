package com.novoda.downloadmanager;

final class FallbackDownloadFileIdProvider {

    private FallbackDownloadFileIdProvider() {
        // non instantiable
    }

    static DownloadFileId downloadFileIdFor(DownloadBatchId downloadBatchId, BatchFile batchFile) {
        String fallbackId = downloadBatchId.rawId() + batchFile.networkAddress();
        return batchFile.downloadFileId().or(DownloadFileIdCreator.createFrom(fallbackId));
    }
}
