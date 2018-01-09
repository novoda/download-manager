package com.novoda.downloadmanager;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

class DownloadsFilePersistence {

    private final DownloadsPersistence downloadsPersistence;

    DownloadsFilePersistence(DownloadsPersistence downloadsPersistence) {
        this.downloadsPersistence = downloadsPersistence;
    }

    // We need to persist all of this information.
    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
    void persistSync(DownloadBatchId downloadBatchId,
                     FileName fileName,
                     FilePath filePath,
                     FileSize fileSize,
                     String url,
                     DownloadFileId downloadFileId,
                     FilePersistenceType filePersistenceType,
                     long downloadDateTimeInMillis) {
        LiteDownloadsFilePersisted filePersisted = new LiteDownloadsFilePersisted(
                downloadBatchId,
                downloadFileId,
                fileName,
                filePath,
                fileSize.totalSize(),
                url,
                filePersistenceType,
                downloadDateTimeInMillis
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
            long downloadDateTimeInMillis = filePersisted.downloadDateTimeInMillis();

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
                    filePath,
                    downloadDateTimeInMillis
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
                    downloadsFilePersistence,
                    downloadDateTimeInMillis
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
            case DELETION:
                return InternalDownloadFileStatus.Status.DELETION;
            case DOWNLOADED:
                return InternalDownloadFileStatus.Status.DOWNLOADING;
            default:
                throw new InvalidParameterException("Batch status " + batchStatus + " is unsupported");
        }
    }
}
