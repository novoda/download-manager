package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to aggregate multiple {@link DownloadBatchRequirementRule}
 * and expose it as a single {@link DownloadBatchRequirementRule},
 * allowing calling code to identify which of the component rules was violated, if any.
 */
final class DownloadBatchRequirementRules {

    private final List<DownloadBatchRequirementRule> rules;

    static DownloadBatchRequirementRules newInstance() {
        return new DownloadBatchRequirementRules(new ArrayList<>());
    }

    private DownloadBatchRequirementRules(List<DownloadBatchRequirementRule> rules) {
        this.rules = rules;
    }

    void addRule(DownloadBatchRequirementRule storageRequirementRule) {
        rules.add(storageRequirementRule);
    }

    public boolean hasViolatedRule(DownloadBatchStatus downloadBatchStatus) {
        return getViolatedRule(downloadBatchStatus).isPresent();
    }

    /**
     * Determines which of the component rules (of which the object consists) has been violated
     *
     * @param downloadBatchStatus of batch to check.
     * @return an {@link Optional} of the {@link DownloadBatchRequirementRule} that was violated,
     * or {@link Optional#absent()} if none
     */
    public Optional<DownloadBatchRequirementRule> getViolatedRule(DownloadBatchStatus downloadBatchStatus) {
        for (DownloadBatchRequirementRule requirementRule : rules) {
            if (requirementRule.hasViolatedRule(downloadBatchStatus)) {
                return Optional.of(requirementRule);
            }
        }

        return Optional.absent();
    }
}
