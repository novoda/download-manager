package com.novoda.downloadmanager;

class UnlinkedDataRemover {

    private final DownloadsPersistence database;
    private final LocalFilesDirectory localFilesDirectory;

    UnlinkedDataRemover(DownloadsPersistence database, LocalFilesDirectory localFilesDirectory) {
        this.database = database;
        this.localFilesDirectory = localFilesDirectory;
    }

    void remove() {
//        List<String> fileNames = database.fileNames();
//
//        for (String fileName : localFilesDirectory.contents()) {
//            if (!fileNames.contains(fileName)) {
//                localFilesDirectory.deleteFile(fileName);
//            }
//        }
    }

}
