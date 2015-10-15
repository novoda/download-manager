package com.novoda.downloadmanager.lib;

import com.novoda.downloadmanager.Download;

class PublicFacingDownloadMarshaller {

    private final StatusTranslator statusTranslator;

    public PublicFacingDownloadMarshaller(StatusTranslator statusTranslator) {
        this.statusTranslator = statusTranslator;
    }

    public Download marshall(DownloadBatch downloadBatch) {
        long batchId = downloadBatch.getBatchId();
        String title = downloadBatch.getInfo().getTitle();
        String description = downloadBatch.getInfo().getDescription();
        int status = statusTranslator.translate(downloadBatch.getStatus());
        long currentSize = downloadBatch.getCurrentSize();
        long totalSize = downloadBatch.getTotalSize();
        return new Download(batchId, title, description, status, currentSize, totalSize);
    }

}
