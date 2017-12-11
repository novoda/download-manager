package com.novoda.downloadmanager;

class UnlinkedDataRemover {
    private final LocalFilesDirectory localFilesDirectory;
    private final V2DatabaseFiles v2DatabaseFiles;

    UnlinkedDataRemover(LocalFilesDirectory localFilesDirectory, V2DatabaseFiles v2DatabaseFiles) {
        this.localFilesDirectory = localFilesDirectory;
        this.v2DatabaseFiles = v2DatabaseFiles;
    }

    void remove() {
        for (String filename : localFilesDirectory.contents()) {
            if (!v2DatabaseFiles.databaseContents().contains(filename)) {
                localFilesDirectory.deleteFile(filename);
            }
        }
    }

}
