package com.novoda.downloadmanager;

public class DownloadBatchRequirementRulesImplFixtures {
    static DownloadBatchRequirementRules withRules(DownloadBatchRequirementRule... rules) {
        DownloadBatchRequirementRules downloadBatchRequirementRules = DownloadBatchRequirementRules.newInstance();
        for (DownloadBatchRequirementRule rule : rules) {
            downloadBatchRequirementRules.addRule(rule);
        }
        return downloadBatchRequirementRules;
    }
}
