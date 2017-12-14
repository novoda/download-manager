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
        List<DownloadsFilePersisted> filesInV2Database = downloadsPersistence.loadAllFiles();
        List<String> databaseFileNames = new ArrayList<>();

        for (DownloadsFilePersisted filePersisted : filesInV2Database) {
            databaseFileNames.add(filePersisted.fileName().name());
        }

        for (String localFile : localFilesDirectory.contents()) {
            if (!databaseFileNames.contains(localFile)) {
                localFilesDirectory.deleteFile(localFile);
            }
        }
    }
}
