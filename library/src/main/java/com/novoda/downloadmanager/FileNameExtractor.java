package com.novoda.downloadmanager;

final class FileNameExtractor {

    private static final String PATH_SEPARATOR = "/";

    private FileNameExtractor() {
        // Uses static method.
    }

    static String extractFrom(String assetUrl) {
        String[] subPaths = assetUrl.split(PATH_SEPARATOR);
        return subPaths.length == 0 ? assetUrl : subPaths[subPaths.length - 1];
    }

}
