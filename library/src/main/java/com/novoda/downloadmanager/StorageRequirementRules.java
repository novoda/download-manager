package com.novoda.downloadmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class StorageRequirementRules implements StorageRequirementRule {

    private final List<StorageRequirementRule> rules;

    static StorageRequirementRules newInstance() {
        return new StorageRequirementRules(new ArrayList<>());
    }

    private StorageRequirementRules(List<StorageRequirementRule> rules) {
        this.rules = rules;
    }

    void addRule(StorageRequirementRule storageRequirementRule) {
        rules.add(storageRequirementRule);
    }

    @Override
    public boolean hasViolatedRule(File storageDirectory, FileSize downloadFileSize) {
        for (StorageRequirementRule requirementRule : rules) {
            if (requirementRule.hasViolatedRule(storageDirectory, downloadFileSize)) {
                return true;
            }
        }

        return false;
    }
}
