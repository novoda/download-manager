package com.novoda.downloadmanager;

/**
 * TODO: comments
 */
public interface DownloadBatchRequirementCompositeRule extends DownloadBatchRequirementRule {
    Optional<DownloadBatchRequirementRule> getViolatedRule(DownloadBatchStatus downloadBatchStatus);
}
