package com.novoda.downloadmanager;

import javax.annotation.Nullable;

/**
 * Used to define the rules that need to be satisfied to download an batch.
 * Clients of this library can create their own custom implementations and
 * pass them to {@link DownloadManagerBuilder#withDownloadBatchRequirementRules(DownloadBatchRequirementRule...)}.
 */
public interface DownloadBatchRequirementRule {

    /**
     * Determines whether a {@link DownloadBatch} has violated a size requirement rule.
     *
     * @param downloadBatchStatus of batch to check.
     * @return whether the {@link DownloadBatch} has violated the rule.
     */
    boolean hasViolatedRule(DownloadBatchStatus downloadBatchStatus);

    /**
     * Uniquely identifies the rule, allowing client code to distinguish which exact rule
     * has been violated if multiple rules were applied.
     *
     * Optional - leave as [null] for no identification
     */
    @Nullable
    Integer getCode();
}
