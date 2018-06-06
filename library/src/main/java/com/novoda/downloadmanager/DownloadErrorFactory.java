package com.novoda.downloadmanager;

class DownloadErrorFactory {

    private DownloadErrorFactory() {
        // Uses static factory methods.
    }

    static DownloadError createSizeMismatchError(DownloadFileStatus downloadFileStatus) {
        String sizeMismatchMessage = String.format(
                "Download File with ID: %s has a greater current size: %s than the total size: ",
                downloadFileStatus.downloadBatchId().rawId(),
                downloadFileStatus.bytesDownloaded(),
                downloadFileStatus.totalBytes()
        );
        return new DownloadError(DownloadError.Type.FILE_CURRENT_AND_TOTAL_SIZE_MISMATCH, sizeMismatchMessage);
    }

}
