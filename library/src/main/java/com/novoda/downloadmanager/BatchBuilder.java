package com.novoda.downloadmanager;

/**
 * Builds instances of {@link Batch} using a fluent API.
 */
public interface BatchBuilder {

    /**
     * Sets {@link BatchFileBuilder} to build a {@link Batch} that will download a {@link BatchFile}
     * from a given  networkAddress.
     *
     * @param networkAddress to download file from.
     * @return {@link BatchFileBuilder}.
     */
    BatchFileBuilder downloadFrom(String networkAddress);

    /**
     * Build a new {@link Batch} instance.
     *
     * @return an instance of {@link Batch}.
     */
    Batch build();

    static BatchBuilder from(Batch batch) {
        InternalBatchBuilder builder = (InternalBatchBuilder) Batch.with(batch.storageRoot(), batch.downloadBatchId(), batch.title());
        for (BatchFile batchFile : batch.batchFiles()) {
            builder.withFile(batchFile);
        }
        return builder;
    }
}
