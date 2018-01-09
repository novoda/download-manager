package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<DownloadFileId, String> fileUrls = batch.fileUrlsByDownloadFileId();
        List<DownloadFile> downloadFiles = new ArrayList<>(fileUrls.size());
        DownloadBatchId downloadBatchId = batch.downloadBatchId();

        for (Map.Entry<DownloadFileId, String> urlByDownloadId : fileUrls.entrySet()) {
            InternalFileSize fileSize = InternalFileSizeCreator.unknownFileSize();
            FilePath filePath = FilePathCreator.unknownFilePath();
            DownloadFileId downloadFileId = urlByDownloadId.getKey();
            InternalDownloadFileStatus downloadFileStatus = new LiteDownloadFileStatus(
                    downloadBatchId,
                    downloadFileId,
                    InternalDownloadFileStatus.Status.QUEUED,
                    fileSize,
                    filePath
            );
            String fileUrl = urlByDownloadId.getValue();
            FileName fileName = LiteFileName.from(batch, fileUrl);

            FilePersistenceCreator filePersistenceCreator = fileOperations.filePersistenceCreator();
            FileDownloader fileDownloader = fileOperations.fileDownloader();
            FileSizeRequester fileSizeRequester = fileOperations.fileSizeRequester();

            FilePersistence filePersistence = filePersistenceCreator.create();
            DownloadFile downloadFile = new DownloadFile(
                    downloadBatchId,
                    downloadFileId,
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
                new HashMap<>(),
                liteDownloadBatchStatus,
                downloadsBatchPersistence,
                callbackThrottle
        );
    }
}
