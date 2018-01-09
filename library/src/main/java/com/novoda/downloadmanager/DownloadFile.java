package com.novoda.downloadmanager;

import com.novoda.downloadmanager.DownloadError.Error;
import com.novoda.notils.logger.simple.Log;

// This model knows how to interact with low level components.
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
class DownloadFile {

    private final DownloadBatchId downloadBatchId;
    private final DownloadFileId downloadFileId;
    private final String url;
    private final InternalDownloadFileStatus downloadFileStatus;
    private final FileName fileName;
    private final FileDownloader fileDownloader;
    private final FileSizeRequester fileSizeRequester;
    private final FilePersistence filePersistence;
    private final DownloadsFilePersistence downloadsFilePersistence;

    private InternalFileSize fileSize;
    private FilePath filePath;

    // Model that knows how to interact with low-level components.
    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
    DownloadFile(DownloadBatchId downloadBatchId,
                 DownloadFileId downloadFileId,
                 String url,
                 InternalDownloadFileStatus downloadFileStatus,
                 FileName fileName,
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
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileDownloader = fileDownloader;
        this.fileSizeRequester = fileSizeRequester;
        this.filePersistence = filePersistence;
        this.fileSize = fileSize;
        this.downloadsFilePersistence = downloadsFilePersistence;
    }

    void download(final Callback callback) {
        callback.onUpdate(downloadFileStatus);

        moveStatusToDownloadingIfQueued();

        fileSize = requestTotalFileSizeIfNecessary(fileSize);

        if (fileSize.isTotalSizeUnknown()) {
            updateAndFeedbackWithStatus(Error.FILE_TOTAL_SIZE_REQUEST_FAILED, callback);
            return;
        }

        FilePersistenceResult result = createFile();
        if (result.isMarkedAsError()) {
            Error error = convertError(result.status());
            updateAndFeedbackWithStatus(error, callback);
            return;
        }

        filePath = result.filePath();
        fileSize.setCurrentSize(filePersistence.getCurrentSize());

        persistSync();

        if (fileSize.currentSize() == fileSize.totalSize()) {
            downloadFileStatus.update(fileSize, filePath);
            callback.onUpdate(downloadFileStatus);
            return;
        }

        fileDownloader.startDownloading(url, fileSize, new FileDownloader.Callback() {
            @Override
            public void onBytesRead(byte[] buffer, int bytesRead) {
                boolean success = filePersistence.write(buffer, 0, bytesRead);
                if (!success) {
                    updateAndFeedbackWithStatus(Error.FILE_CANNOT_BE_WRITTEN, callback);
                }

                if (downloadFileStatus.isMarkedAsDownloading()) {
                    fileSize.addToCurrentSize(bytesRead);
                    downloadFileStatus.update(fileSize, filePath);
                    callback.onUpdate(downloadFileStatus);
                }
            }

            @Override
            public void onError() {
                updateAndFeedbackWithStatus(Error.NETWORK_ERROR_CANNOT_DOWNLOAD_FILE, callback);
            }

            @Override
            public void onDownloadFinished() {
                filePersistence.close();
                if (downloadFileStatus.isMarkedForDeletion()) {
                    filePersistence.delete();
                }
            }
        });
    }

    private FilePersistenceResult createFile() {
        if (filePath.isUnknown()) {
            return filePersistence.create(fileName, fileSize);

        } else {
            return filePersistence.create(filePath);
        }
    }

    private Error convertError(FilePersistenceResult.Status status) {
        switch (status) {
            case SUCCESS:
                Log.e("Cannot convert success status to any DownloadError type");
                break;
            case ERROR_UNKNOWN_TOTAL_FILE_SIZE:
                return DownloadError.Error.FILE_TOTAL_SIZE_REQUEST_FAILED;
            case ERROR_INSUFFICIENT_SPACE:
                return DownloadError.Error.FILE_CANNOT_BE_CREATED_LOCALLY_INSUFFICIENT_FREE_SPACE;
            case ERROR_EXTERNAL_STORAGE_NON_WRITABLE:
                return DownloadError.Error.STORAGE_UNAVAILABLE;
            case ERROR_OPENING_FILE:
                return DownloadError.Error.FILE_CANNOT_BE_WRITTEN;
            default:
                Log.e("Status " + status + " missing to be processed");
                break;

        }

        return DownloadError.Error.UNKNOWN;
    }

    private InternalFileSize requestTotalFileSizeIfNecessary(InternalFileSize fileSize) {
        InternalFileSize updatedFileSize = fileSize.copy();

        if (fileSize.isTotalSizeUnknown()) {
            FileSize requestFileSize = fileSizeRequester.requestFileSize(url);
            if (requestFileSize.isTotalSizeKnown()) {
                updatedFileSize.setTotalSize(requestFileSize.totalSize());
            }
        }

        return updatedFileSize;
    }

    private void updateAndFeedbackWithStatus(Error error, Callback callback) {
        downloadFileStatus.markAsError(error);
        callback.onUpdate(downloadFileStatus);
    }

    private void moveStatusToDownloadingIfQueued() {
        if (downloadFileStatus.isMarkedAsQueued()) {
            downloadFileStatus.markAsDownloading();
        }
    }

    void pause() {
        downloadFileStatus.isMarkedAsPaused();
        fileDownloader.stopDownloading();
    }

    void resume() {
        downloadFileStatus.markAsQueued();
    }

    void delete() {
        if (downloadFileStatus.isMarkedAsDownloading()) {
            downloadFileStatus.markForDeletion();
            fileDownloader.stopDownloading();
        } else {
            filePersistence.delete();
        }
    }

    long getTotalSize() {
        if (fileSize.isTotalSizeUnknown()) {
            FileSize requestFileSize = fileSizeRequester.requestFileSize(url);
            fileSize.setTotalSize(requestFileSize.totalSize());
            persistSync();
        }

        return fileSize.totalSize();
    }

    void persistSync() {
        downloadsFilePersistence.persistSync(
                downloadBatchId,
                fileName,
                filePath,
                fileSize,
                url,
                downloadFileStatus.downloadFileId(),
                filePersistence.getType()
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
