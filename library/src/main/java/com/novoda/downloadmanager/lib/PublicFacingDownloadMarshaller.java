package com.novoda.downloadmanager.lib;

import com.novoda.downloadmanager.Download;

class PublicFacingDownloadMarshaller {

    public Download marshall(DownloadBatch downloadBatch) {
        long batchId = downloadBatch.getBatchId();
        String title = downloadBatch.getInfo().getTitle();
        String description = downloadBatch.getInfo().getDescription();
        long currentSize = downloadBatch.getCurrentSize();
        long totalSize = downloadBatch.getTotalSize();
        return new Download(batchId, title, description, currentSize, totalSize);
    }

}
