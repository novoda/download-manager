package com.novoda.downloadmanager;

final class FilePathExtractor {

    private static final String PATH_SEPARATOR = "/";
    private static final String EMPTY = "";

    static DownloadFilePath extractFrom(String basePath, String filePathToExtractFrom) {
        String relativePath = removeSubstring(filePathToExtractFrom, basePath);
        String fileName = getFileName(filePathToExtractFrom);
        return new DownloadFilePath(basePath, relativePath, LiteFileName.from(fileName));
    }

    private static String getFileName(String assetUri) {
        String[] subPaths = assetUri.split(PATH_SEPARATOR);
        return subPaths.length == 0 ? assetUri : subPaths[subPaths.length - 1];
    }

    private static String removeSubstring(String source, String subString) {
        return source.replaceAll(subString, EMPTY);
    }

    static class DownloadFilePath {

        private final String basePath;
        private final String relativePath;
        private final FileName fileName;

        DownloadFilePath(String basePath, String relativePath, FileName fileName) {
            this.basePath = basePath;
            this.relativePath = relativePath;
            this.fileName = fileName;
        }

        public String basePath() {
            return basePath;
        }

        public String relativePath() {
            return relativePath;
        }

        public FileName fileName() {
            return fileName;
        }

        public String absolutePath() {
            return basePath + relativePath;
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

            if (basePath != null ? !basePath.equals(that.basePath) : that.basePath != null) {
                return false;
            }
            if (relativePath != null ? !relativePath.equals(that.relativePath) : that.relativePath != null) {
                return false;
            }
            return fileName != null ? fileName.equals(that.fileName) : that.fileName == null;
        }

        @Override
        public int hashCode() {
            int result = basePath != null ? basePath.hashCode() : 0;
            result = 31 * result + (relativePath != null ? relativePath.hashCode() : 0);
            result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "DownloadFilePath{" +
                    "basePath='" + basePath + '\'' +
                    ", relativePath='" + relativePath + '\'' +
                    ", fileName=" + fileName +
                    '}';
        }
    }
}
