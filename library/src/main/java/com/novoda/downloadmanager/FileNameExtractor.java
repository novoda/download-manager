package com.novoda.downloadmanager;

final class FileNameExtractor {

    private static final String PATH_SEPARATOR = "/";

    private FileNameExtractor() {
        // Uses static method.
    }

    static FileName extractFrom(String assetUrl) {
        String rawFileName = getRawFileName(assetUrl);
        return LiteFileName.from(rawFileName);
    }

    private static String getRawFileName(String assetUrl) {
        String[] subPaths = assetUrl.split(PATH_SEPARATOR);
        return subPaths.length == 0 ? assetUrl : subPaths[subPaths.length - 1];
    }

}
