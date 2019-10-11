package com.novoda.downloadmanager;

public class DownloadBatchRequirementRulesImplFixtures {
    static DownloadBatchRequirementRulesImpl withRules(DownloadBatchRequirementRule... rules) {
        DownloadBatchRequirementRulesImpl downloadBatchRequirementRules = DownloadBatchRequirementRulesImpl.newInstance();
        for (DownloadBatchRequirementRule rule : rules) {
            downloadBatchRequirementRules.addRule(rule);
        }
        return downloadBatchRequirementRules;
    }
}
