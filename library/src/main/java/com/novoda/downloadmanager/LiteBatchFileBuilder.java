package com.novoda.downloadmanager;

import java.io.File;

final class LiteBatchFileBuilder implements InternalBatchFileBuilder {

    private final DownloadBatchId downloadBatchId;
    private final String networkAddress;

    private Optional<DownloadFileId> downloadFileId = Optional.absent();
    private String path;
    private String fileName;

    private InternalBatchBuilder parentBuilder;

    LiteBatchFileBuilder(DownloadBatchId downloadBatchId, String networkAddress) {
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
        this.path = path;
        this.fileName = fileName;

        return this;
    }

    @Override
    public BatchBuilder apply() {
        StringBuilder stringBuilder = new StringBuilder(path);
        if (!path.endsWith("/")) {
            stringBuilder.append("/");
        }

        if (fileName.startsWith("/")) {
            fileName = fileName.replaceFirst("/", "");
        }
        stringBuilder.append(downloadBatchId.rawId());
        stringBuilder.append(File.separatorChar);
        stringBuilder.append(fileName);

        parentBuilder.withFile(new BatchFile(networkAddress, downloadFileId, stringBuilder.toString()));
        return parentBuilder;
    }

}
