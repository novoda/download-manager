package com.novoda.downloadmanager;

class FileNameExtractor {

    private static final String PATH_SEPARATOR = "/";

    static FileName extractFrom(String assetUrl) {
        String rawFileName = getRawFileName(assetUrl);
        return LiteFileName.from(rawFileName);
    }

    private static String getRawFileName(String assetUrl) {
        String[] subPaths = assetUrl.split(PATH_SEPARATOR);
        return subPaths.length == 0 ? assetUrl : subPaths[subPaths.length - 1];
    }

}
