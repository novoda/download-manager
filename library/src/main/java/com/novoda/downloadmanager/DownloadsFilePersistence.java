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

            FilePath filePath = filePersisted.filePath();
            LiteDownloadFileStatus downloadFileStatus = new LiteDownloadFileStatus(
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

    private LiteDownloadFileStatus.Status getFileStatusFrom(DownloadBatchStatus.Status batchStatus) {
        switch (batchStatus) {
            case QUEUED:
                return LiteDownloadFileStatus.Status.QUEUED;
            case DOWNLOADING:
                return LiteDownloadFileStatus.Status.DOWNLOADING;
            case PAUSED:
                return LiteDownloadFileStatus.Status.PAUSED;
            case ERROR:
                return LiteDownloadFileStatus.Status.ERROR;
            case DELETION:
                return LiteDownloadFileStatus.Status.DELETION;
            case DOWNLOADED:
                return LiteDownloadFileStatus.Status.DOWNLOADING;
            default:
                throw new InvalidParameterException("Batch status " + batchStatus + " is unsupported");
        }
    }
}
