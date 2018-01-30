package com.novoda.downloadmanager;

public final class FilePathCreator {

    private static final String EMPTY = "";
    private static final String UNKNOWN = "unknown";
    private static final FilePath UNKNOWN_FILEPATH = new LiteFilePath(UNKNOWN);

    private FilePathCreator() {
        // Uses static factory methods.
    }

    public static FilePath unknownFilePath() {
        return UNKNOWN_FILEPATH;
    }

    public static FilePath create(String basePath, String rawPath) {
        String relativePath = removeSubstring(rawPath, basePath);
        String absolutePath = basePath + relativePath;
        return new LiteFilePath(absolutePath);
    }

    private static String removeSubstring(String source, String subString) {
        return source.replaceAll(subString, EMPTY);
    }

}
