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
            FileName fileNameFromNetworkAddress = FileNameExtractor.extractFrom(fileNetworkAddress);

            String relativePath = networkAddressAndFilePath.relativePathToStoreDownload();

            FilePersistenceCreator filePersistenceCreator = fileOperations.filePersistenceCreator();
            FilePersistence filePersistence = filePersistenceCreator.create();

            FilePathExtractor.DownloadFilePath downloadFilePath = FilePathExtractor.extractFrom(filePersistence.basePath().path(), relativePath == null || relativePath.isEmpty() ? fileNameFromNetworkAddress.name() : relativePath);
            FilePath filePath = FilePathCreator.create(downloadFilePath.absolutePath());

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
                    downloadFilePath.fileName(),
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
}
