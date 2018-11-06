package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

final class DownloadBatchRequirementRules implements DownloadBatchRequirementRule {

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

    @Override
    public boolean hasViolatedRule(DownloadBatchStatus downloadBatchStatus, Long batchSize) {
        for (DownloadBatchRequirementRule requirementRule : rules) {
            if (requirementRule.hasViolatedRule(downloadBatchStatus, batchSize)) {
                return true;
            }
        }

        return false;
    }
}
