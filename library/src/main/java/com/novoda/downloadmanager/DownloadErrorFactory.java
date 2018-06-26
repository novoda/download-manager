package com.novoda.downloadmanager;

final class DownloadErrorFactory {

    private DownloadErrorFactory() {
        // Uses static factory methods.
    }

    static DownloadError createSizeMismatchError(DownloadFileStatus downloadFileStatus) {
        String sizeMismatchMessage = "Download File with ID: "
                + downloadFileStatus.downloadBatchId().rawId()
                + " has a greater current size: "
                + downloadFileStatus.bytesDownloaded()
                + " than the total size: "
                + downloadFileStatus.totalBytes();

        return new DownloadError(DownloadError.Type.FILE_CURRENT_AND_TOTAL_SIZE_MISMATCH, sizeMismatchMessage);
    }

    static DownloadError createTotalSizeRequestFailedError(DownloadFileId downloadFileId, String url) {
        String totalSizeRequestFailedMessage = "Total size request failed for File with ID: "
                + downloadFileId.rawId()
                + " and Request: "
                + url;

        return new DownloadError(DownloadError.Type.FILE_TOTAL_SIZE_REQUEST_FAILED, totalSizeRequestFailedMessage);
    }

    static DownloadError createInsufficientFreeSpaceError(DownloadFileStatus downloadFileStatus) {
        String insufficientFreeSpaceMessage =
                "Insufficient free space to create file with ID: "
                        + downloadFileStatus.downloadFileId().rawId()
                        + " Bytes Required: "
                        + downloadFileStatus.totalBytes();

        return new DownloadError(DownloadError.Type.FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE, insufficientFreeSpaceMessage);
    }

    static DownloadError createCannotWriteToFileError(DownloadFileStatus downloadFileStatus) {
        String cannotWriteToFileMessage = "Cannot write to file with Id: " + downloadFileStatus.downloadFileId().rawId();
        return new DownloadError(DownloadError.Type.FILE_CANNOT_BE_WRITTEN, cannotWriteToFileMessage);
    }

    static DownloadError createStorageNotAvailableError(FilePath filePath) {
        // QUESTION: is it enough to swap out the basePath with the filePath?
        String storageUnavailableMessage = "Storage unavailable to save file to: " + filePath;
        return new DownloadError(DownloadError.Type.STORAGE_UNAVAILABLE, storageUnavailableMessage);
    }

    static DownloadError createNetworkError(String networkErrorCause) {
        String networkErrorMessage = "Network error, cannot download file. Cause: " + networkErrorCause;
        return new DownloadError(DownloadError.Type.NETWORK_ERROR_CANNOT_DOWNLOAD_FILE, networkErrorMessage);
    }

    static DownloadError createUnknownErrorFor(FilePersistenceResult status) {
        String unknownErrorMessage = "Unhandled error for FilePersistenceResult: " + status.name();
        return new DownloadError(DownloadError.Type.UNKNOWN, unknownErrorMessage);
    }
}
