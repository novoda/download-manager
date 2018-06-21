package com.novoda.downloadmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class StorageRequirementRules implements StorageRequirementRule {

    private List<StorageRequirementRule> storageRequirementRules;

    static StorageRequirementRules newInstance() {
        return new StorageRequirementRules(new ArrayList<>());
    }

    private StorageRequirementRules(List<StorageRequirementRule> storageRequirementRules) {
        this.storageRequirementRules = storageRequirementRules;
    }

    void addRule(StorageRequirementRule storageRequirementRule) {
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
