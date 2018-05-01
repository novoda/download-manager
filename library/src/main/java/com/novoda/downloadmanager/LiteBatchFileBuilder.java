package com.novoda.downloadmanager;

import java.io.File;

final class LiteBatchFileBuilder implements InternalBatchFileBuilder {

    private final DownloadBatchId downloadBatchId;
    private final String networkAddress;

    private Optional<DownloadFileId> downloadFileId = Optional.absent();
    private Optional<String> path = Optional.absent();

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
        if (path != null && fileName != null) {
            this.path = Optional.of(path + fileName);
        }

        return this;
    }

    @Override
    public BatchBuilder apply() {
        String networkAddressDerivedFileName = FileNameExtractor.extractFrom(networkAddress);
        String pathPrependedWithBatchId = prependBatchIdTo(path.or(networkAddressDerivedFileName), downloadBatchId);

        parentBuilder.withFile(new BatchFile(networkAddress, downloadFileId, pathPrependedWithBatchId));
        return parentBuilder;
    }

    private static String prependBatchIdTo(String filePath, DownloadBatchId downloadBatchId) {
        return downloadBatchId.rawId() + File.separatorChar + filePath;
    }

}
