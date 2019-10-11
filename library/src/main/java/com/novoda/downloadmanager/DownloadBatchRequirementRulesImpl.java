package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

final class DownloadBatchRequirementRulesImpl implements DownloadBatchRequirementRules {

    private final List<DownloadBatchRequirementRule> rules;

    static DownloadBatchRequirementRulesImpl newInstance() {
        return new DownloadBatchRequirementRulesImpl(new ArrayList<>());
    }

    private DownloadBatchRequirementRulesImpl(List<DownloadBatchRequirementRule> rules) {
        this.rules = rules;
    }

    void addRule(DownloadBatchRequirementRule storageRequirementRule) {
        rules.add(storageRequirementRule);
    }

    @Override
    public boolean hasViolatedRule(DownloadBatchStatus downloadBatchStatus) {
        return getViolatedRule(downloadBatchStatus).isPresent();
    }

    @Override
    public Optional<DownloadBatchRequirementRule> getViolatedRule(DownloadBatchStatus downloadBatchStatus) {
        for (DownloadBatchRequirementRule requirementRule : rules) {
            if (requirementRule.hasViolatedRule(downloadBatchStatus)) {
                return Optional.of(requirementRule);
            }
        }

        return Optional.absent();
    }

    /**
     * @return [null] - a composite rule isn't identifiable by itself.
     * Retrieve the code of the actually violated rule (if any) via {@link #getViolatedRule#getCode()}.
     */
    @Nullable
    @Override
    public Integer getCode() {
        return null;
    }
}
