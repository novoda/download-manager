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
        Map<DownloadFileId, NetworkAddressAndFilePath> networkAddressAndFileNameById = batch.networkAddressAndFileNameById();
        List<DownloadFile> downloadFiles = new ArrayList<>(networkAddressAndFileNameById.size());
        DownloadBatchId downloadBatchId = batch.getDownloadBatchId();
        long downloadedDateTimeInMillis = System.currentTimeMillis();

        for (Map.Entry<DownloadFileId, NetworkAddressAndFilePath> networkAddressAndFilePathByDownloadId : networkAddressAndFileNameById.entrySet()) {
            NetworkAddressAndFilePath networkAddressAndFilePath = networkAddressAndFilePathByDownloadId.getValue();

            InternalFileSize fileSize = InternalFileSizeCreator.unknownFileSize();

            String fileNetworkAddress = networkAddressAndFilePath.networkAddress();

            FilePersistenceCreator filePersistenceCreator = fileOperations.filePersistenceCreator();
            FilePersistence filePersistence = filePersistenceCreator.create();

            String basePath = filePersistence.basePath().path();
            FilePath filePath = FilePathCreator.create(basePath, relativePathFrom(networkAddressAndFilePath));
            FileName fileName = FileNameExtractor.extractFrom(filePath.path());

            DownloadFileId downloadFileId = networkAddressAndFilePathByDownloadId.getKey();
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
                    fileNetworkAddress,
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

    private static String relativePathFrom(NetworkAddressAndFilePath networkAddressAndFilePath) {
        String relativePath = networkAddressAndFilePath.relativePathToStoreDownload();
        String fileNameFromNetworkAddress = FileNameExtractor.extractFrom(networkAddressAndFilePath.networkAddress()).name();
        return relativePath == null || relativePath.isEmpty() ? fileNameFromNetworkAddress : relativePath;
    }

}
