package com.novoda.downloadmanager;

class UnlinkedDataRemover {
    
    private final LocalFilesDirectory localFilesDirectory;
    private final V2DatabaseFiles downloadFiles;

    UnlinkedDataRemover(LocalFilesDirectory localFilesDirectory, V2DatabaseFiles downloadFiles) {
        this.localFilesDirectory = localFilesDirectory;
        this.downloadFiles = downloadFiles;
    }

    void remove() {
        for (String fileName : localFilesDirectory.contents()) {
            if (!downloadFiles.fileNames().contains(fileName)) {
                localFilesDirectory.deleteFile(fileName);
            }
        }
    }

}
