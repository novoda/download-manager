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
        this.path = Optional.fromNullable(path).filterNot("/"::equals);
        this.fileName = Optional.fromNullable(fileName);
        return this;
    }

    @Override
    public BatchBuilder apply() {
        String absolutePath = storageRoot.path() + File.separator
                + downloadBatchId.rawId() + File.separator
                + path.map((it) -> it + File.separator).or("")
                + fileName.getOrElse(() -> FileNameExtractor.extractFrom(networkAddress));

        parentBuilder.withFile(new BatchFile(networkAddress, downloadFileId, absolutePath));
        return parentBuilder;
    }

}
