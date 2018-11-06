package com.novoda.downloadmanager;

/**
 * Used to define the rules that need to be satisfied to download an batch.
 * Clients of this library can create their own custom implementations and
 * pass them to {@link DownloadManagerBuilder#withStorageRequirementRules(StorageRequirementRule...)}.
 */
public interface DownloadBatchRequirementRule {

    /**
     * Determines whether a {@link DownloadBatch} has violated a size requirement rule.
     *
     * @param batchSize of the batch to check.
     * @return whether the {@link DownloadBatch} has violated the rule.
     */
    boolean hasViolatedRule(Long batchSize);
}
