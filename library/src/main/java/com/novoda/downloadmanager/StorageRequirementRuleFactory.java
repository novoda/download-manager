package com.novoda.downloadmanager;

import android.support.annotation.FloatRange;

public final class StorageRequirementRuleFactory {

    private StorageRequirementRuleFactory() {
        // Uses static factory methods.
    }

    public static StorageRequirementRule createByteBasedRule(long bytesRemainingAfterDownload) {
        return new ByteBasedStorageRequirementRule(new StorageCapacityReader(), bytesRemainingAfterDownload);
    }

    public static StorageRequirementRule createPercetageBasedRule(@FloatRange(from = 0.0, to = 0.5) float percentageOfStorageRemaining) {
        return new PercentageBasedStorageRequirementRule(new StorageCapacityReader(), percentageOfStorageRemaining);
    }
}
