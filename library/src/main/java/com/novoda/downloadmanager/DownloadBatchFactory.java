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
        DownloadBatchId downloadBatchId = batch.downloadBatchId();
        long downloadedDateTimeInMillis = System.currentTimeMillis();

        List<DownloadFile> downloadFiles = new ArrayList<>(batch.files().size());

        for (File file : batch.files()) {
            String networkAddress = file.networkAddress();

            InternalFileSize fileSize = InternalFileSizeCreator.unknownFileSize();

            FilePersistenceCreator filePersistenceCreator = fileOperations.filePersistenceCreator();
            FilePersistence filePersistence = filePersistenceCreator.create();

            String basePath = filePersistence.basePath().path();
            FilePath filePath = FilePathCreator.create(basePath, relativePathFrom(file));
            FileName fileName = FileNameExtractor.extractFrom(filePath.path());

            DownloadFileId downloadFileId = downloadFileIdFrom(batch, file);
            InternalDownloadFileStatus downloadFileStatus = new LiteDownloadFileStatus(
                    downloadBatchId,
                    downloadFileId,
                    InternalDownloadFileStatus.Status.QUEUED,
                    fileSize,
                    filePath
            );

            FileDownloader fileDownloader = fileOperations.fileDownloader();
            FileSizeRequester fileSizeRequester = fileOperations.fileSizeRequester();

            DownloadFile downloadFile = new DownloadFile(
                    downloadBatchId,
                    downloadFileId,
                    networkAddress,
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
                downloadedDateTimeInMillis,
                DownloadBatchStatus.Status.QUEUED
        );

        return new DownloadBatch(
                liteDownloadBatchStatus,
                downloadFiles,
                new HashMap<>(),
                downloadsBatchPersistence,
                callbackThrottle
        );
    }

    private static String relativePathFrom(File file) {
        String fileNameFromNetworkAddress = FileNameExtractor.extractFrom(file.networkAddress()).name();
        return file.relativePath().or(fileNameFromNetworkAddress);
    }

    private static DownloadFileId downloadFileIdFrom(Batch batch, File file) {
        String rawId = batch.downloadBatchId().rawId() + file.networkAddress();
        return file.downloadFileId().or(DownloadFileIdCreator.createFrom(rawId));
    }

}
