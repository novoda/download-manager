package com.novoda.downloadmanager;

import android.support.annotation.WorkerThread;

// This model knows how to interact with low level components.
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
class DownloadFile {

    private final DownloadBatchId downloadBatchId;
    private final DownloadFileId downloadFileId;
    private final String url;
    private final InternalDownloadFileStatus downloadFileStatus;
    private final FileDownloader fileDownloader;
    private final FileSizeRequester fileSizeRequester;
    private final FilePersistence filePersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;
    private final FilePath filePath;

    private InternalFileSize fileSize;

    // Model that knows how to interact with low-level components.
    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
    DownloadFile(DownloadBatchId downloadBatchId,
                 DownloadFileId downloadFileId,
                 String url,
                 InternalDownloadFileStatus downloadFileStatus,
                 FilePath filePath,
                 InternalFileSize fileSize,
                 FileDownloader fileDownloader,
                 FileSizeRequester fileSizeRequester,
                 FilePersistence filePersistence,
                 DownloadsFilePersistence downloadsFilePersistence) {
        this.downloadBatchId = downloadBatchId;
        this.downloadFileId = downloadFileId;
        this.url = url;
        this.downloadFileStatus = downloadFileStatus;
        this.filePath = filePath;
        this.fileDownloader = fileDownloader;
        this.fileSizeRequester = fileSizeRequester;
        this.filePersistence = filePersistence;
        this.fileSize = fileSize;
        this.downloadsFilePersistence = downloadsFilePersistence;
    }

    // This ia complex because we have to constantly check states and perform updates.
    @SuppressWarnings("PMD.NPathComplexity")
    void download(Callback callback) {
        downloadFileStatus.markAsDownloading();

        callback.onUpdate(downloadFileStatus);

        InternalFileSize updatedFileSize = fileSize.copy();

        if (fileSize.isTotalSizeUnknown()) {
            FileSizeResult fileSizeResult = fileSizeRequester.requestFileSize(url);
            if (fileSizeResult.isSuccess()) {
                updatedFileSize.setTotalSize(fileSizeResult.fileSize().totalSize());
                fileSize = updatedFileSize;
            } else {
                DownloadError downloadError = DownloadErrorFactory.createTotalSizeRequestFailedError(downloadFileId, url, fileSizeResult.failureMessage());
                updateAndFeedbackWithStatus(downloadError, callback);
                return;
            }
        }

        fileSize.setCurrentSize(filePersistence.getCurrentSize(filePath));

        if (downloadFileStatus.isMarkedAsDeleted()) {
            return;
        }

        Logger.v("persist file " + downloadFileId.rawId() + ", with status: " + downloadFileStatus.status());
        if (!persist()) {
            Logger.e("persisting file " + downloadFileId.rawId() + " with status " + downloadFileStatus.status() + " failed");
            return;
        }

        if (fileSize.currentSize() == fileSize.totalSize()) {
            downloadFileStatus.update(fileSize, filePath);
            callback.onUpdate(downloadFileStatus);
            return;
        }

        FilePersistenceResult result = filePersistence.create(filePath, fileSize);
        if (result != FilePersistenceResult.SUCCESS) {
            DownloadError downloadError = convertError(result);
            updateAndFeedbackWithStatus(downloadError, callback);
            return;
        }

        fileDownloader.startDownloading(url, fileSize, new FileDownloader.Callback() {
            @Override
            public void onBytesRead(byte[] buffer, int bytesRead) {
                boolean success = filePersistence.write(buffer, 0, bytesRead);
                if (!success) {
                    DownloadError downloadError = DownloadErrorFactory.createCannotWriteToFileError(downloadFileStatus);
                    updateAndFeedbackWithStatus(downloadError, callback);
                }

                if (downloadFileStatus.isMarkedAsDownloading()) {
                    fileSize.addToCurrentSize(bytesRead);
                    downloadFileStatus.update(fileSize, filePath);
                    callback.onUpdate(downloadFileStatus);
                }
            }

            @Override
            public void onError(String cause) {
                DownloadError downloadError = DownloadErrorFactory.createNetworkError(cause);
                updateAndFeedbackWithStatus(downloadError, callback);
            }

            @Override
            public void onDownloadFinished() {
                filePersistence.close();
                if (downloadFileStatus.isMarkedAsDeleted()) {
                    filePersistence.delete(filePath);
                }
                if (downloadFileStatus.isMarkedAsWaitingForNetwork()) {
                    callback.onUpdate(downloadFileStatus);
                }
            }
        });
    }

    private DownloadError convertError(FilePersistenceResult status) {
        switch (status) {
            case ERROR_UNKNOWN_TOTAL_FILE_SIZE:
                return DownloadErrorFactory.createTotalSizeRequestFailedError(downloadFileId, url);
            case ERROR_INSUFFICIENT_SPACE:
                return DownloadErrorFactory.createInsufficientFreeSpaceError(downloadFileStatus);
            case ERROR_OPENING_FILE:
                return DownloadErrorFactory.createCannotWriteToFileError(downloadFileStatus);
            default:
                Logger.e("Status " + status + " missing to be processed");
                return DownloadErrorFactory.createUnknownErrorFor(status);
        }
    }

    private void updateAndFeedbackWithStatus(DownloadError downloadError, Callback callback) {
        downloadFileStatus.markAsError(downloadError);
        callback.onUpdate(downloadFileStatus);
    }

    void pause() {
        downloadFileStatus.markAsPaused();
        fileDownloader.stopDownloading();
    }

    void resume() {
        downloadFileStatus.markAsQueued();
    }

    void waitForNetwork() {
        downloadFileStatus.waitForNetwork();
        fileDownloader.stopDownloading();
    }

    void delete() {
        if (downloadFileStatus.isMarkedAsDownloading()) {
            downloadFileStatus.markAsDeleted();
            Logger.v("mark file as deleted for batchId: " + downloadBatchId.rawId());
            fileDownloader.stopDownloading();
        } else {
            downloadFileStatus.markAsDeleted();
            Logger.v("mark file as deleted for batchId: " + downloadBatchId.rawId());
            filePersistence.delete(filePath);
        }
    }

    @WorkerThread
    long getTotalSize() {
        if (fileSize.isTotalSizeUnknown()) {
            FileSizeResult fileSizeResult = fileSizeRequester.requestFileSize(url);
            if (fileSizeResult.isSuccess()) {
                fileSize.setTotalSize(fileSizeResult.fileSize().totalSize());
            }

            if (fileStatus().status() == DownloadFileStatus.Status.DELETED) {
                Logger.e("file getTotalSize return zero because is deleted, " + downloadFileId.rawId()
                                 + " from batch " + downloadBatchId.rawId()
                                 + " with file status " + fileStatus().status());
                return 0;
            }
            persist();
        }

        return fileSize.totalSize();
    }

    @WorkerThread
    boolean persist() {
        return downloadsFilePersistence.persistSync(
                downloadBatchId,
                filePath,
                fileSize,
                url,
                downloadFileStatus
        );
    }

    long getCurrentDownloadedBytes() {
        return fileSize.currentSize();
    }

    DownloadFileId id() {
        return downloadFileStatus.downloadFileId();
    }

    boolean matches(DownloadFileId downloadFileId) {
        return this.downloadFileId.equals(downloadFileId);
    }

    DownloadFileStatus fileStatus() {
        return downloadFileStatus;
    }

    interface Callback {

        void onUpdate(InternalDownloadFileStatus downloadFileStatus);
    }
}
