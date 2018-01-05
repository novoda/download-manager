package com.novoda.downloadmanager;

class FileOperations {

    private final FilePersistenceCreator filePersistenceCreator;
    private final FileSizeRequester fileSizeRequester;
    private final FileDownloader fileDownloader;

    FileOperations(FilePersistenceCreator filePersistenceCreator, FileSizeRequester fileSizeRequester, FileDownloader fileDownloader) {
        this.filePersistenceCreator = filePersistenceCreator;
        this.fileSizeRequester = fileSizeRequester;
        this.fileDownloader = fileDownloader;
    }

    FilePersistenceCreator filePersistenceCreator() {
        return filePersistenceCreator;
    }

    FileSizeRequester fileSizeRequester() {
        return fileSizeRequester;
    }

    FileDownloader fileDownloader() {
        return fileDownloader;
    }
}
