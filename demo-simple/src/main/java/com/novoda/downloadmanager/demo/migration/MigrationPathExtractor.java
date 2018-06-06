package com.novoda.downloadmanager.demo.migration;

import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.FilePath;
import com.novoda.downloadmanager.FilePathCreator;

import java.io.File;

public final class MigrationPathExtractor {

    private static final String PATH_SEPARATOR = File.separator;
    private static final String EMPTY = "";
    private static final String BASE_PATH = "";

    private MigrationPathExtractor() {
        // Uses static utility methods.
    }

    public static FilePath extractMigrationPath(String basePath, String assetPath, DownloadBatchId downloadBatchId) {
        String relativePath = extractRelativePath(basePath, assetPath);
        String relativePathWithBatchId = prependBatchIdTo(relativePath, downloadBatchId);
        String fileName = extractFileName(assetPath);
        String absolutePath = basePath + PATH_SEPARATOR + relativePathWithBatchId + fileName;
        String sanitizedAbsolutePath = absolutePath.replaceAll("//", PATH_SEPARATOR);
        return FilePathCreator.create(BASE_PATH, sanitizedAbsolutePath);
    }

    private static String extractRelativePath(String basePath, String assetPath) {
        String subPathWithFileName = removeSubstring(assetPath, basePath);
        String fileName = extractFileName(subPathWithFileName);
        return removeSubstring(subPathWithFileName, fileName);
    }

    private static String prependBatchIdTo(String filePath, DownloadBatchId downloadBatchId) {
        return sanitizeBatchIdPath(downloadBatchId.rawId()) + File.separatorChar + filePath;
    }

    private static String sanitizeBatchIdPath(String batchIdPath) {
        return batchIdPath.replaceAll("[:\\\\/*?|<>]", "_");
    }

    private static String extractFileName(String assetUri) {
        String[] subPaths = assetUri.split(regexUsablePathSeparator());
        return subPaths.length == 0 ? assetUri : subPaths[subPaths.length - 1];
    }

    private static String regexUsablePathSeparator() {
        return File.separatorChar == '\\' ? "\\\\" : File.separator;
    }

    private static String removeSubstring(String source, String subString) {
        return source.replaceAll(subString, EMPTY);
    }

}
