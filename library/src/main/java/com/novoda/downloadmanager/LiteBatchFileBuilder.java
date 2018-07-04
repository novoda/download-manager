package com.novoda.downloadmanager;

import java.io.File;

final class LiteBatchFileBuilder implements InternalBatchFileBuilder {

    private final StorageRoot storageRoot;
    private final DownloadBatchId downloadBatchId;
    private final String networkAddress;

    private Optional<DownloadFileId> downloadFileId = Optional.absent();
    private String path;
    private String fileName;

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
        this.path = path;
        this.fileName = fileName;
        return this;
    }

    @Override
    public BatchBuilder apply() {
        if (fileName == null) {
            fileName = FileNameExtractor.extractFrom(networkAddress);
        }

        StringBuilder absolutePath = new StringBuilder(storageRoot.path());

        absolutePath = absolutePath.append(File.separatorChar)
                .append(downloadBatchId.rawId())
                .append(File.separatorChar);

        if (path != null) {
            absolutePath = absolutePath.append(path)
                    .append(File.separatorChar);
        }

        absolutePath = absolutePath.append(fileName);

        parentBuilder.withFile(new BatchFile(networkAddress, downloadFileId, absolutePath.toString()));
        return parentBuilder;
    }

}
