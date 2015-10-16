package com.novoda.downloadmanager.lib;

import com.novoda.downloadmanager.Download;

/**
 * The idea is we don't want to expose DownloadBatch on the public api methods,
 * so we always convert to a `Download` before passing externally
 *
 * This allows us to keep a private api for DownloadBatch and change it as we please
 */
public class PublicFacingDownloadMarshaller {

    public Download marshall(DownloadBatch downloadBatch) {
        long batchId = downloadBatch.getBatchId();
        String title = downloadBatch.getInfo().getTitle();
        String description = downloadBatch.getInfo().getDescription();
        long currentSize = downloadBatch.getCurrentSize();
        long totalSize = downloadBatch.getTotalSize();
        return new Download(batchId, title, description, currentSize, totalSize);
    }

}
