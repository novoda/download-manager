package com.novoda.downloadmanager;

class FileOperations {

    private final FilePersistenceCreator filePersistenceCreator;
    private final FileSizeRequester fileSizeRequester;
    private final FileDownloaderCreator fileDownloaderCreator;

    FileOperations(FilePersistenceCreator filePersistenceCreator, FileSizeRequester fileSizeRequester, FileDownloaderCreator fileDownloaderCreator) {
        this.filePersistenceCreator = filePersistenceCreator;
        this.fileSizeRequester = fileSizeRequester;
        this.fileDownloaderCreator = fileDownloaderCreator;
    }

    FilePersistenceCreator filePersistenceCreator() {
        return filePersistenceCreator;
    }

    FileSizeRequester fileSizeRequester() {
        return fileSizeRequester;
    }

    FileDownloaderCreator fileDownloaderCreator() {
        return fileDownloaderCreator;
    }
}
