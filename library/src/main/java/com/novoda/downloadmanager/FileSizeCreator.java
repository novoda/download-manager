package com.novoda.downloadmanager;

public final class FileSizeCreator {

    private static final int ZERO_BYTES = 0;
    private static final FileSize UNKNOWN = new LiteFileSize(ZERO_BYTES, ZERO_BYTES);

    private FileSizeCreator() {
        // Uses static factory methods.
    }

    static FileSize unknownFileSize() {
        return UNKNOWN;
    }

    public static FileSize createFromTotalSize(long totalFileSize) {
        return new LiteFileSize(ZERO_BYTES, totalFileSize);
    }
}
