package com.novoda.downloadmanager.demo;

import com.novoda.downloadmanager.DownloadBatchRequirementRule;
import com.novoda.downloadmanager.DownloadBatchStatus;

public class DownloadBatchSizeRequirementRule implements DownloadBatchRequirementRule {

    public final static int CODE = DownloadBatchRequirementRule.class.hashCode();

    private final DemoBatchSizeProvider batchSizeProvider;

    public DownloadBatchSizeRequirementRule(DemoBatchSizeProvider batchSizeProvider) {
        this.batchSizeProvider = batchSizeProvider;
    }

    @Override
    public boolean hasViolatedRule(DownloadBatchStatus downloadBatchStatus) {
        return batchSizeProvider.getMaxSizeOfBatch() < downloadBatchStatus.bytesTotalSize();
    }

    @Override
    public Integer getCode() {
        return CODE;
    }
}
