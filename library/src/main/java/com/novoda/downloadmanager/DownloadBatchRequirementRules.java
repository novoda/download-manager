package com.novoda.downloadmanager;

/**
 * Used to aggregate multiple {@link DownloadBatchRequirementRule}
 * and expose it as a single {@link DownloadBatchRequirementRule},
 * allowing calling code to identify which of the component rules was violated, if any.
 */
public interface DownloadBatchRequirementRules extends DownloadBatchRequirementRule {

    /**
     * Determines which of the component rules (of which the object consists) has been violated
     *
     * @param downloadBatchStatus of batch to check.
     * @return an {@link Optional} of the {@link DownloadBatchRequirementRule} that was violated,
     * or {@link Optional#absent()} if none
     */
    Optional<DownloadBatchRequirementRule> getViolatedRule(DownloadBatchStatus downloadBatchStatus);
}
