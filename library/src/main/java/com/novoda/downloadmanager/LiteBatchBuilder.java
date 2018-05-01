package com.novoda.downloadmanager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class LiteBatchBuilder implements InternalBatchBuilder {

    private final DownloadBatchId downloadBatchId;
    private final String title;
    private final List<BatchFile> batchFiles;

    LiteBatchBuilder(DownloadBatchId downloadBatchId, String title, List<BatchFile> batchFiles) {
        this.downloadBatchId = downloadBatchId;
        this.title = title;
        this.batchFiles = batchFiles;
    }

    @Override
    public void withFile(BatchFile batchFile) {
        batchFiles.add(batchFile);
    }

    @Override
    public BatchFileBuilder downloadFrom(String networkAddress) {
        return BatchFile.from(downloadBatchId, networkAddress).withParentBuilder(this);
    }

    @Override
    public Batch build() {
        ensureNoFileIdDuplicates(batchFiles);
        return new Batch(downloadBatchId, title, batchFiles);
    }

    private void ensureNoFileIdDuplicates(List<BatchFile> batchFiles) {
        Set<DownloadFileId> rawIdsWithoutDuplicates = new HashSet<>();
        for (BatchFile batchFile : batchFiles) {
            rawIdsWithoutDuplicates.add(FallbackDownloadFileIdProvider.downloadFileIdFor(downloadBatchId, batchFile));
        }

        if (rawIdsWithoutDuplicates.size() != batchFiles.size()) {
            throw new IllegalArgumentException(String.format("Duplicated file for batch %s (batchId: %s)", title, downloadBatchId.rawId()));
        }
    }

}
