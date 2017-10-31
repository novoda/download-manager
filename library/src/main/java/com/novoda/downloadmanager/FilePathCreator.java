package com.novoda.downloadmanager;

public class FilePathCreator {

    private static final String UNKNOWN = "unknown";

    static final FilePath UNKNOWN_FILEPATH = new LiteFilePath(UNKNOWN);

    public static FilePath create(String absolutePath) {
        return new LiteFilePath(absolutePath);
    }
}
