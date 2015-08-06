package com.novoda.downloadmanager.lib;

import com.novoda.downloadmanager.Download;

class PublicFacingDownloadMarshaller {

    public Download marshall(DownloadBatch downloadBatch) {
        return new Download(downloadBatch.getBatchId(), downloadBatch.getCurrentSize(), downloadBatch.getTotalSize());
    }

}
