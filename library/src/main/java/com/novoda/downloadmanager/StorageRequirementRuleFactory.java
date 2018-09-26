package com.novoda.downloadmanager;

import android.support.annotation.FloatRange;

public final class StorageRequirementRuleFactory {

    private StorageRequirementRuleFactory() {
        // Uses static factory methods.
    }

    /**
     * Creates a storage requirement rule, where the storage bytes must be greater than the
     * given bytes after a download completes. E.g. if specifying 100MB then the rule will
     * be violated if the system storage remaining after the download is 99MB, preventing the
     * download from being started.
     *
     * @param bytesRemainingAfterDownload the amount of storage in bytes that must be remaining after a download completes.
     * @return the storage requirement rule to be evaluated when creating a file.
     */
    public static StorageRequirementRule createByteBasedRule(long bytesRemainingAfterDownload) {
        return new ByteBasedRemainingStorageRequirementRule(new StorageCapacityReader(), bytesRemainingAfterDownload);
    }

    /**
     * Creates a storage requirement rule, where the storage bytes must be greater than the
     * a given percentage of the storage bytes after a download completes. E.g. if you specify 10% then the rule
     * will be violated if the remaining system storage is at 9% of the total system storage, preventing the download
     * from being started.
     *
     * @param percentageOfStorageRemaining the amount of storage, as a percentage of the system storage, that must
     *                                     be remaining after a download completes.
     * @return the storage requirement rule to be evaluated when creating a file.
     */
    public static StorageRequirementRule createPercentageBasedRule(@FloatRange(from = 0.0, to = 0.5) float percentageOfStorageRemaining) {
        return new PercentageBasedRemainingStorageRequirementRule(new StorageCapacityReader(), percentageOfStorageRemaining);
    }
}
