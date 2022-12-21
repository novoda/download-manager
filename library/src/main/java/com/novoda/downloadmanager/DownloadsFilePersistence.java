package com.novoda.downloadmanager;

import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.WorkerThread;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

class DownloadsFilePersistence {

    private final DownloadsPersistence downloadsPersistence;

    DownloadsFilePersistence(DownloadsPersistence downloadsPersistence) {
        this.downloadsPersistence = downloadsPersistence;
    }

    @WorkerThread
    boolean persistSync(DownloadBatchId downloadBatchId,
                        FilePath filePath,
                        FileSize fileSize,
                        String url,
                        DownloadFileStatus downloadFileStatus) {
        if (downloadFileStatus.status() == DownloadFileStatus.Status.DELETED) {
            return false;
        }
        DownloadsFilePersisted filePersisted = new LiteDownloadsFilePersisted(
                downloadBatchId,
                downloadFileStatus.downloadFileId(),
                filePath,
                fileSize.totalSize(),
                url
        );

        downloadsPersistence.startTransaction();
        try {
            downloadsPersistence.persistFile(filePersisted);
            downloadsPersistence.transactionSuccess();
            return true;
        } catch (SQLiteConstraintException e) {
            Logger.e("failure to persist sync file " + downloadFileStatus.downloadFileId().rawId() + " with status " + downloadFileStatus.status());
            return false;
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

            FilePersistenceCreator filePersistenceCreator = fileOperations.filePersistenceCreator();
            FilePersistence filePersistence = filePersistenceCreator.create();

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
            FileDownloaderCreator fileDownloaderCreator = fileOperations.fileDownloaderCreator();
            FileDownloader fileDownloader = fileDownloaderCreator.create();

            DownloadFile downloadFile = new DownloadFile(
                    batchId,
                    downloadFileId,
                    url,
                    downloadFileStatus,
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

    @SuppressWarnings("PMD.CyclomaticComplexity")
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
            case DELETING:
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
