package com.novoda.downloadmanager;

import java.io.File;

class UnlinkedDataRemover {
    private final UnlinkedDataRemoverTest.LocalFilesDirectory localFilesDirectory;
    private final UnlinkedDataRemoverTest.V2DatabaseFiles v2DatabaseFiles;

    UnlinkedDataRemover(UnlinkedDataRemoverTest.LocalFilesDirectory localFilesDirectory, UnlinkedDataRemoverTest.V2DatabaseFiles v2DatabaseFiles) {
        this.localFilesDirectory = localFilesDirectory;
        this.v2DatabaseFiles = v2DatabaseFiles;
    }

    void remove() {
        for (File file : localFilesDirectory.contents()) {
            if (v2DatabaseFiles.databaseContents().contains(file)) {
                //
            } else {
                file.delete();
            }
        }
    }
}
