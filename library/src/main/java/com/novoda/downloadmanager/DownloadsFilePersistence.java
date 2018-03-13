package com.novoda.downloadmanager;

import android.support.annotation.WorkerThread;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

class DownloadsFilePersistence {

    private final DownloadsPersistence downloadsPersistence;
    private final Executor executor;

    DownloadsFilePersistence(DownloadsPersistence downloadsPersistence, Executor executor) {
        this.downloadsPersistence = downloadsPersistence;
        this.executor = executor;
    }

    void persistAsync(DownloadBatchId downloadBatchId,
                      FileName fileName,
                      FilePath filePath,
                      FileSize fileSize,
                      String url,
                      DownloadFileStatus downloadFileStatus,
                      FilePersistenceType filePersistenceType) {
        executor.execute(() -> persistSync(downloadBatchId, fileName, filePath, fileSize, url, downloadFileStatus, filePersistenceType));
    }

    @WorkerThread
    void persistSync(DownloadBatchId downloadBatchId,
                     FileName fileName,
                     FilePath filePath,
                     FileSize fileSize,
                     String url,
                     DownloadFileStatus downloadFileStatus,
                     FilePersistenceType filePersistenceType) {
        if (downloadFileStatus.status() == DownloadFileStatus.Status.DELETED) {
            return;
        }
        LiteDownloadsFilePersisted filePersisted = new LiteDownloadsFilePersisted(
                downloadBatchId,
                downloadFileStatus.downloadFileId(),
                fileName,
                filePath,
                fileSize.totalSize(),
                url,
                filePersistenceType
        );

        downloadsPersistence.startTransaction();
        try {
            downloadsPersistence.persistFile(filePersisted);
            downloadsPersistence.transactionSuccess();
        } finally {
            downloadsPersistence.endTransaction();
        }
    }

    List<DownloadFile> loadSync(DownloadBatchId batchId,
                                DownloadBatchStatus.Status batchStatus,
                                FileOperations fileOperations,
                                DownloadsFilePersistence downloadsFilePersistence) {
        List<DownloadsFilePersisted> filePersistedList = downloadsPersistence.loadFiles(batchId);

        List<DownloadFile> downloadFiles = new ArrayList<>(filePersistedList.size());
        for (DownloadsFilePersisted filePersisted : filePersistedList) {
            DownloadFileId downloadFileId = filePersisted.downloadFileId();
            FileName fileName = filePersisted.fileName();

            FilePersistenceCreator filePersistenceCreator = fileOperations.filePersistenceCreator();
            FilePersistence filePersistence = filePersistenceCreator.create(filePersisted.filePersistenceType());

            long currentSize = filePersistence.getCurrentSize(filePersisted.filePath());
            long totalFileSize = filePersisted.totalFileSize();
            InternalFileSize fileSize = InternalFileSizeCreator.createFromCurrentAndTotalSize(currentSize, totalFileSize);
            String url = filePersisted.url();

            FilePath filePath = filePersisted.filePath();
            InternalDownloadFileStatus downloadFileStatus = new LiteDownloadFileStatus(
                    batchId,
                    downloadFileId,
                    getFileStatusFrom(batchStatus),
                    fileSize,
                    filePath
            );

            FileSizeRequester fileSizeRequester = fileOperations.fileSizeRequester();
            FileDownloader fileDownloader = fileOperations.fileDownloader();

            DownloadFile downloadFile = new DownloadFile(
                    batchId,
                    downloadFileId,
                    url,
                    downloadFileStatus,
                    fileName,
                    filePath,
                    fileSize,
                    fileDownloader,
                    fileSizeRequester,
                    filePersistence,
                    downloadsFilePersistence
            );

            downloadFiles.add(downloadFile);
        }

        return downloadFiles;
    }

    private InternalDownloadFileStatus.Status getFileStatusFrom(DownloadBatchStatus.Status batchStatus) {
        switch (batchStatus) {
            case QUEUED:
                return InternalDownloadFileStatus.Status.QUEUED;
            case DOWNLOADING:
                return InternalDownloadFileStatus.Status.DOWNLOADING;
            case PAUSED:
                return InternalDownloadFileStatus.Status.PAUSED;
            case ERROR:
                return InternalDownloadFileStatus.Status.ERROR;
            case DELETED:
                return InternalDownloadFileStatus.Status.DELETED;
            case DOWNLOADED:
                return InternalDownloadFileStatus.Status.DOWNLOADED;
            case WAITING_FOR_NETWORK:
                return InternalDownloadFileStatus.Status.WAITING_FOR_NETWORK;
            default:
                throw new InvalidParameterException("Batch status " + batchStatus + " is unsupported");
        }
    }
}
