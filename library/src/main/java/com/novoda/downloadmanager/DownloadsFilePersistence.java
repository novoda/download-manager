package com.novoda.downloadmanager;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

class DownloadsFilePersistence {

    private final DownloadsPersistence downloadsPersistence;

    DownloadsFilePersistence(DownloadsPersistence downloadsPersistence) {
        this.downloadsPersistence = downloadsPersistence;
    }

    void persistSync(DownloadBatchId downloadBatchId,
                     FileName fileName,
                     FilePath filePath,
                     FileSize fileSize,
                     String url,
                     DownloadFileId downloadFileId,
                     FilePersistenceType filePersistenceType) {
        LiteDownloadsFilePersisted filePersisted = new LiteDownloadsFilePersisted(
                downloadBatchId,
                downloadFileId,
                fileName,
                filePath,
                fileSize.totalSize(),
                url,
                filePersistenceType
        );

        downloadsPersistence.persistFile(filePersisted);
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

            DownloadFileStatus downloadFileStatus = new DownloadFileStatus(
                    downloadFileId,
                    getFileStatusFrom(batchStatus),
                    fileSize
            );

            FileSizeRequester fileSizeRequester = fileOperations.fileSizeRequester();
            FileDownloader fileDownloader = fileOperations.fileDownloader();
            FilePath filePath = filePersisted.filePath();

            DownloadFile downloadFile = new DownloadFile(
                    batchId,
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

    private DownloadFileStatus.Status getFileStatusFrom(DownloadBatchStatus.Status batchStatus) {
        switch (batchStatus) {
            case QUEUED:
                return DownloadFileStatus.Status.QUEUED;
            case DOWNLOADING:
                return DownloadFileStatus.Status.DOWNLOADING;
            case PAUSED:
                return DownloadFileStatus.Status.PAUSED;
            case ERROR:
                return DownloadFileStatus.Status.ERROR;
            case DELETION:
                return DownloadFileStatus.Status.DELETION;
            case DOWNLOADED:
                return DownloadFileStatus.Status.DOWNLOADING;
            default:
                throw new InvalidParameterException("Batch status " + batchStatus + " is unsupported");
        }
    }
}
