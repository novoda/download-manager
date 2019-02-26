package com.novoda.downloadmanager;

import java.io.File;

final class LiteBatchFileBuilder implements InternalBatchFileBuilder {

    private final StorageRoot storageRoot;
    private final DownloadBatchId downloadBatchId;
    private final String networkAddress;

    private Optional<DownloadFileId> downloadFileId = Optional.absent();
    private Optional<String> path = Optional.absent();
    private Optional<String> fileName = Optional.absent();

    private InternalBatchBuilder parentBuilder;

    LiteBatchFileBuilder(StorageRoot storageRoot, DownloadBatchId downloadBatchId, String networkAddress) {
        this.storageRoot = storageRoot;
        this.downloadBatchId = downloadBatchId;
        this.networkAddress = networkAddress;
    }

    @Override
    public BatchFileBuilder withParentBuilder(InternalBatchBuilder parentBuilder) {
        this.parentBuilder = parentBuilder;
        return this;
    }

    @Override
    public BatchFileBuilder withIdentifier(DownloadFileId downloadFileId) {
        this.downloadFileId = Optional.fromNullable(downloadFileId);
        return this;
    }

    @Override
    public BatchFileBuilder saveTo(String path) {
        String networkAddressDerivedFileName = FileNameExtractor.extractFrom(networkAddress);
        return saveTo(path, networkAddressDerivedFileName);
    }

    @Override
    public BatchFileBuilder saveTo(String path, String fileName) {
        this.path = Optional.fromNullable(path);
        this.fileName = Optional.fromNullable(fileName);
        return this;
    }

    @Override
    public BatchBuilder apply() {
        String absolutePath = buildPath(
                storageRoot.path(),
                downloadBatchId.rawId(),
                path.or(""),
                fileName.getOrElse(() -> FileNameExtractor.extractFrom(networkAddress))
        );

        parentBuilder.withFile(new BatchFile(networkAddress, downloadFileId, absolutePath));
        return parentBuilder;
    }

    private String buildPath(String... paths) {
        StringBuilder stringBuilder = new StringBuilder();

        String storagePath = paths[0];
        if (storagePath.startsWith(File.separator)) {
            stringBuilder.append(File.separator);
        }

        for (String path : paths) {
            if (path.isEmpty()) {
                continue; // ignore empty paths
            }
            if (!isLastCharFileSeparator(stringBuilder)) {
                stringBuilder.append(File.separatorChar);
            }
            stringBuilder.append(removeLeadingTrailingFileSeparator(path));
        }

        return stringBuilder.toString();
    }

    private String removeLeadingTrailingFileSeparator(String element) {
        int beginIndex = 0;
        int endIndex = element.length();

        if (element.charAt(0) == File.separatorChar) {
            beginIndex = 1;
        }
        int lastIndexOfSeparator = element.lastIndexOf(File.separatorChar);
        if (lastIndexOfSeparator != 0 && lastIndexOfSeparator == element.length() - 1) {
            endIndex = element.length() - 1;
        }
        return element.substring(beginIndex, endIndex);
    }

    private boolean isLastCharFileSeparator(StringBuilder stringBuilder) {
        return stringBuilder.length() <= 0 || stringBuilder.charAt(stringBuilder.length() - 1) == File.separatorChar;
    }

}
