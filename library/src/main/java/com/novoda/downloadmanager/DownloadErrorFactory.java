package com.novoda.downloadmanager;

class DownloadErrorFactory {

    private static final String NO_MESSAGE = "";

    private DownloadErrorFactory() {
        // Uses static factory methods.
    }

    static DownloadError createSizeMismatchError(DownloadFileStatus downloadFileStatus) {
        String sizeMismatchMessage = String.format(
                "Download File with ID: %s has a greater current size: %s than the total size: %s",
                downloadFileStatus.downloadBatchId().rawId(),
                downloadFileStatus.bytesDownloaded(),
                downloadFileStatus.totalBytes()
        );
        return new DownloadError(DownloadError.Type.FILE_CURRENT_AND_TOTAL_SIZE_MISMATCH, sizeMismatchMessage);
    }

    static DownloadError createTotalSizeRequestFailedError(DownloadFileId downloadFileId, String url) {
        String totalSizeRequestFailedMessage = String.format(
                "Total size request failed for File with ID: %s and Request: %s",
                downloadFileId.rawId(),
                url
        );
        return new DownloadError(DownloadError.Type.FILE_TOTAL_SIZE_REQUEST_FAILED, totalSizeRequestFailedMessage);
    }

    static DownloadError createInsufficientFreeSpaceError(DownloadFileStatus downloadFileStatus) {
        String insufficientFreeSpaceMessage = String.format(
                "Insufficient free space to create file with ID: %s Bytes Required: %s",
                downloadFileStatus.downloadFileId().rawId(),
                downloadFileStatus.totalBytes()
        );
        return new DownloadError(DownloadError.Type.FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE, insufficientFreeSpaceMessage);
    }

    static DownloadError createCannotWriteToFileError(DownloadFileStatus downloadFileStatus) {
        String cannotWriteToFileMessage = String.format(
                "Cannot write to file with Id: %s",
                downloadFileStatus.downloadFileId().rawId()
        );
        return new DownloadError(DownloadError.Type.FILE_CANNOT_BE_WRITTEN, cannotWriteToFileMessage);
    }

    static DownloadError createStorageNotAvailableError(FilePersistence filePersistence) {
        String storageUnavailableMessage = String.format(
                "Storage with base path: %s is not available",
                filePersistence.basePath()
        );
        return new DownloadError(DownloadError.Type.STORAGE_UNAVAILABLE, storageUnavailableMessage);
    }

    static DownloadError createNetworkError(String networkErrorCause) {
        String networkErrorMessage = String.format(
                "Network error, cannot download file. Cause: %s",
                networkErrorCause
        );
        return new DownloadError(DownloadError.Type.STORAGE_UNAVAILABLE, networkErrorMessage);
    }

    static DownloadError createUnknownError() {
        String unknownErrorMessage = "Unknown download error, additional information unavailable.";
        return new DownloadError(DownloadError.Type.UNKNOWN, unknownErrorMessage);
    }

}
