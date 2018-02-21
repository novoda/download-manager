package com.novoda.downloadmanager;

public enum FilePersistenceResult {

    SUCCESS,
    ERROR_UNKNOWN_TOTAL_FILE_SIZE,
    ERROR_INSUFFICIENT_SPACE,
    ERROR_EXTERNAL_STORAGE_NON_WRITABLE,
    ERROR_OPENING_FILE
}
