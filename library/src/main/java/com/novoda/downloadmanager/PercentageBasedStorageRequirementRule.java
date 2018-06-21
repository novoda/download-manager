package com.novoda.downloadmanager;

import android.support.annotation.FloatRange;

import java.io.File;

public final class PercentageBasedStorageRequirementRule implements StorageRequirementRule {

    private final StorageCapacityReader storageCapacityReader;
    private final float percentageOfStorageRemaining;

    public static PercentageBasedStorageRequirementRule withPercentageOfStorageRemaining(@FloatRange(from = 0.0, to = 0.5) float percentageOfStorageRemaining) {
        return new PercentageBasedStorageRequirementRule(new StorageCapacityReader(), percentageOfStorageRemaining);
    }

    private PercentageBasedStorageRequirementRule(StorageCapacityReader storageCapacityReader,
                                                  @FloatRange(from = 0.0, to = 0.5) float percentageOfStorageRemaining) {
        this.storageCapacityReader = storageCapacityReader;
        this.percentageOfStorageRemaining = percentageOfStorageRemaining;
    }

    @Override
    public boolean hasViolatedRule(File storageDirectory,
                                   FileSize downloadFileSize) {
        long storageCapacityInBytes = storageCapacityReader.storageCapacityInBytes(storageDirectory.getPath());
        long minimumStorageRequiredInBytes = (long) (storageCapacityInBytes * percentageOfStorageRemaining);
        long usableStorageInBytes = storageDirectory.getUsableSpace();
        long remainingStorageAfterDownloadInBytes = usableStorageInBytes - downloadFileSize.totalSize();

        Logger.v("Storage capacity in bytes: ", storageCapacityInBytes);
        Logger.v("Usable storage in bytes: ", usableStorageInBytes);
        Logger.v("Minimum required storage in bytes: ", minimumStorageRequiredInBytes);
        return remainingStorageAfterDownloadInBytes < minimumStorageRequiredInBytes;
    }

}
