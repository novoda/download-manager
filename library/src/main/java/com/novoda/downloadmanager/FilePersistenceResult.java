package com.novoda.downloadmanager;

public final class FilePersistenceResult {

    public enum Status {
        SUCCESS,
        ERROR_UNKNOWN_TOTAL_FILE_SIZE,
        ERROR_INSUFFICIENT_SPACE,
        ERROR_EXTERNAL_STORAGE_NON_WRITABLE,
        ERROR_OPENING_FILE;
    }

    private final Status status;
    private final FilePath filePath;

    public static FilePersistenceResult newInstance(Status status) {
        return new FilePersistenceResult(status, FilePathCreator.unknownFilePath());
    }

    public static FilePersistenceResult newInstance(Status status, FilePath filePath) {
        return new FilePersistenceResult(status, filePath);
    }

    private FilePersistenceResult(Status status, FilePath filePath) {
        this.status = status;
        this.filePath = filePath;
    }

    boolean isMarkedAsError() {
        return status != Status.SUCCESS;
    }

    public Status status() {
        return status;
    }

    public FilePath filePath() {
        return filePath;
    }
}
