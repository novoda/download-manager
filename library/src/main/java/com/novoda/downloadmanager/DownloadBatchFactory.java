package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

final class DownloadBatchFactory {

    private DownloadBatchFactory() {
        // non instantiable factory class
    }

    static DownloadBatch newInstance(Batch batch,
                                     FileOperations fileOperations,
                                     DownloadsBatchPersistence downloadsBatchPersistence,
                                     DownloadsFilePersistence downloadsFilePersistence,
                                     CallbackThrottle callbackThrottle) {
        DownloadBatchTitle downloadBatchTitle = DownloadBatchTitleCreator.createFrom(batch);
        List<String> fileUrls = batch.getFileUrls();
        List<DownloadFile> downloadFiles = new ArrayList<>(fileUrls.size());
        DownloadBatchId downloadBatchId = batch.getDownloadBatchId();

        for (String fileUrl : fileUrls) {
            InternalFileSize fileSize = InternalFileSizeCreator.unknownFileSize();
            DownloadFileId downloadFileId = DownloadFileId.from(batch);
            DownloadFileStatus downloadFileStatus = new DownloadFileStatus(
                    downloadFileId,
                    DownloadFileStatus.Status.QUEUED,
                    fileSize
            );
            FileName fileName = LiteFileName.from(batch, fileUrl);

            FilePersistenceCreator filePersistenceCreator = fileOperations.filePersistenceCreator();
            FileDownloader fileDownloader = fileOperations.fileDownloader();
            FileSizeRequester fileSizeRequester = fileOperations.fileSizeRequester();
            FilePath filePath = FilePathCreator.unknownFilePath();

            FilePersistence filePersistence = filePersistenceCreator.create();
            DownloadFile downloadFile = new DownloadFile(
                    downloadBatchId,
                    fileUrl,
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

        InternalDownloadBatchStatus liteDownloadBatchStatus = new LiteDownloadBatchStatus(
                downloadBatchId,
                downloadBatchTitle,
                DownloadBatchStatus.Status.QUEUED
        );

        return new DownloadBatch(
                downloadBatchTitle,
                downloadBatchId,
                downloadFiles,
                new HashMap<DownloadFileId, Long>(),
                liteDownloadBatchStatus,
                downloadsBatchPersistence,
                callbackThrottle
        );
    }
}
