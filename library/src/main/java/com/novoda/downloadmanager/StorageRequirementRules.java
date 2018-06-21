package com.novoda.downloadmanager;

import java.io.File;
import java.util.List;

class StorageRequirementRules implements StorageRequirementRule {

    private List<StorageRequirementRule> storageRequirementRules;

    public StorageRequirementRules(List<StorageRequirementRule> storageRequirementRules) {
        this.storageRequirementRules = storageRequirementRules;
    }

    public void addRule(StorageRequirementRule storageRequirementRule) {
        storageRequirementRules.add(storageRequirementRule);
    }

    @Override
    public boolean hasViolatedRule(File storageDirectory, FileSize downloadFileSize) {
        for (StorageRequirementRule requirementRule : storageRequirementRules) {
            if (requirementRule.hasViolatedRule(storageDirectory, downloadFileSize)) {
                return true;
            }
        }

        return false;
    }
}
