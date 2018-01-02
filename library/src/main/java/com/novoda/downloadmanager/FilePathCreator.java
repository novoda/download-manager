package com.novoda.downloadmanager;

public final class FilePathCreator {

    private static final String UNKNOWN = "unknown";

    private FilePathCreator() {
        // Uses static factory methods.
    }

    static final FilePath UNKNOWN_FILEPATH = new LiteFilePath(UNKNOWN);

    public static FilePath create(String absolutePath) {
        return new LiteFilePath(absolutePath);
    }
}
