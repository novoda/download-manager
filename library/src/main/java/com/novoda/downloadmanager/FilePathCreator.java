package com.novoda.downloadmanager;

public final class FilePathCreator {

    private static final String UNKNOWN = "unknown";
    private static final FilePath UNKNOWN_FILEPATH = new LiteFilePath(UNKNOWN);

    private FilePathCreator() {
        // Uses static factory methods.
    }

    public static FilePath unknownFilePath() {
        return UNKNOWN_FILEPATH;
    }

    public static FilePath create(String rawPath) {
        return new LiteFilePath(rawPath);
    }
}
