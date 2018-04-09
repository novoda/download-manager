package com.novoda.downloadmanager;

import android.os.StatFs;
import android.support.annotation.FloatRange;

import java.io.File;

public class StorageRequirementsRule {

    private final StorageCapacityReader storageCapacityReader;
    private final float percentageOfStorageRemaining;

    public static StorageRequirementsRule withPercentageOfStorageRemaining(@FloatRange(from = 0.0, to = 0.5) float percentageOfStorageRemaining) {
        return new StorageRequirementsRule(new StorageCapacityReader(), percentageOfStorageRemaining);
    }

    private StorageRequirementsRule(StorageCapacityReader storageCapacityReader,
                                    @FloatRange(from = 0.0, to = 0.5) float percentageOfStorageRemaining) {
        this.storageCapacityReader = storageCapacityReader;
        this.percentageOfStorageRemaining = percentageOfStorageRemaining;
    }

    boolean hasViolatedRule(File storageDirectory,
                            FileSize downloadFileSize) {
        StatFs statFs = new StatFs(storageDirectory.getPath());
        long storageCapacityInBytes = storageCapacityReader.storageCapacityInBytes(statFs);
        long minimumStorageRequiredInBytes = (long) (storageCapacityInBytes * percentageOfStorageRemaining);
        long usableStorageInBytes = storageDirectory.getUsableSpace();
        long remainingStorageAfterDownloadInBytes = usableStorageInBytes - downloadFileSize.totalSize();

        Logger.v("Storage capacity in bytes: ", storageCapacityInBytes);
        Logger.v("Usable storage in bytes: ", usableStorageInBytes);
        Logger.v("Minimum required storage in bytes: ", minimumStorageRequiredInBytes);
        return remainingStorageAfterDownloadInBytes < minimumStorageRequiredInBytes;
    }

}
