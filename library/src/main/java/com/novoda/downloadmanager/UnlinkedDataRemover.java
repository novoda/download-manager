package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

class UnlinkedDataRemover {

    private final DownloadsPersistence downloadsPersistence;
    private final LocalFilesDirectory localFilesDirectory;

    UnlinkedDataRemover(DownloadsPersistence downloadsPersistence, LocalFilesDirectory localFilesDirectory) {
        this.downloadsPersistence = downloadsPersistence;
        this.localFilesDirectory = localFilesDirectory;
    }

    void remove() {
        List<String> filesInV2Database = new ArrayList<>();
        for (DownloadsBatchPersisted batch : downloadsPersistence.loadBatches()) {
            DownloadBatchId downloadBatchId = batch.downloadBatchId();
            for (DownloadsFilePersisted file : downloadsPersistence.loadFiles(downloadBatchId)) {
                String name = file.fileName().name();
                filesInV2Database.add(name);
            }
        }
        for (String localFile : localFilesDirectory.contents()) {
            if (!filesInV2Database.contains(localFile)) {
                localFilesDirectory.deleteFile(localFile);
            }
        }
    }

}
