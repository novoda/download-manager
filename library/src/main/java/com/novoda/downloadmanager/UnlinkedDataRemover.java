package com.novoda.downloadmanager;

import java.util.List;

class UnlinkedDataRemover {

    private final RoomAppDatabase database;
    private final LocalFilesDirectory localFilesDirectory;

    UnlinkedDataRemover(RoomAppDatabase database, LocalFilesDirectory localFilesDirectory) {
        this.database = database;
        this.localFilesDirectory = localFilesDirectory;
    }

    void remove() {
        List<String> fileNames = database.fileNames();

        for (String fileName : localFilesDirectory.contents()) {
            if (!fileNames.contains(fileName)) {
                localFilesDirectory.deleteFile(fileName);
            }
        }
    }

}
