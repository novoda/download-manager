package com.novoda.downloadmanager;

public final class MigrationStoragePathSanitizer {

    private static final String FILE_SCHEME_TO_REMOVE = "file:";
    private static final String EMPTY = "";

    private MigrationStoragePathSanitizer() {
        // Uses static utility methods.
    }

    public static String sanitize(String originalFilePath) {
        return originalFilePath.replace(FILE_SCHEME_TO_REMOVE, EMPTY);
    }

}
