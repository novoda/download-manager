package com.novoda.downloadmanager;

final class FilePathExtractor {

    private static final String PATH_SEPARATOR = "/";
    private static final String EMPTY = "";

    static DownloadFilePath extractFrom(String basePath, String filePathToExtractFrom) {
        String relativePath = removeSubstring(filePathToExtractFrom, basePath);
        FileName fileName = getFileName(filePathToExtractFrom);
        String absolutePath = basePath + relativePath;
        return new DownloadFilePath(absolutePath, fileName);
    }

    private static FileName getFileName(String assetUri) {
        String[] subPaths = assetUri.split(PATH_SEPARATOR);
        return LiteFileName.from(subPaths.length == 0 ? assetUri : subPaths[subPaths.length - 1]);
    }

    private static String removeSubstring(String source, String subString) {
        return source.replaceAll(subString, EMPTY);
    }

    static class DownloadFilePath {

        private final String absolutePath;
        private final FileName fileName;

        DownloadFilePath(String absolutePath, FileName fileName) {
            this.absolutePath = absolutePath;
            this.fileName = fileName;
        }

        public FileName fileName() {
            return fileName;
        }

        public String absolutePath() {
            return absolutePath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DownloadFilePath that = (DownloadFilePath) o;

            if (absolutePath != null ? !absolutePath.equals(that.absolutePath) : that.absolutePath != null) {
                return false;
            }
            return fileName != null ? fileName.equals(that.fileName) : that.fileName == null;
        }

        @Override
        public int hashCode() {
            int result = absolutePath != null ? absolutePath.hashCode() : 0;
            result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "DownloadFilePath{" +
                    "absolutePath='" + absolutePath + '\'' +
                    ", fileName=" + fileName +
                    '}';
        }
    }
}
