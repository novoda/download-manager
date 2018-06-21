package com.novoda.downloadmanager;

import android.os.StatFs;

import java.io.File;

public final class ByteBasedStorageRequirementRule implements StorageRequirementRule {

    private final StorageCapacityReader storageCapacityReader;
    private final long bytesRemainingAfterDownload;

    public static ByteBasedStorageRequirementRule withPercentageOfStorageRemaining(long bytesRemainingAfterDownload) {
        return new ByteBasedStorageRequirementRule(new StorageCapacityReader(), bytesRemainingAfterDownload);
    }

    private ByteBasedStorageRequirementRule(StorageCapacityReader storageCapacityReader, long bytesRemainingAfterDownload) {
        this.storageCapacityReader = storageCapacityReader;
        this.bytesRemainingAfterDownload = bytesRemainingAfterDownload;
    }

    @Override
    public boolean hasViolatedRule(File storageDirectory,
                                   FileSize downloadFileSize) {
        StatFs statFs = new StatFs(storageDirectory.getPath());
        long storageCapacityInBytes = storageCapacityReader.storageCapacityInBytes(statFs);
        long usableStorageInBytes = storageDirectory.getUsableSpace();
        long remainingStorageAfterDownloadInBytes = usableStorageInBytes - downloadFileSize.totalSize();

        Logger.v("Storage capacity in bytes: ", storageCapacityInBytes);
        Logger.v("Usable storage in bytes: ", usableStorageInBytes);
        Logger.v("Minimum required storage in bytes: ", bytesRemainingAfterDownload);
        return remainingStorageAfterDownloadInBytes < bytesRemainingAfterDownload;
    }

}
