package com.novoda.downloadmanager;

public class FileSizeCreator {

    private static final int ZERO_BYTES = 0;

    static FileSize UNKNOWN = new LiteFileSize(ZERO_BYTES, ZERO_BYTES);

    public static FileSize createFromTotalSize(long totalFileSize) {
        return new LiteFileSize(ZERO_BYTES, totalFileSize);
    }
}
