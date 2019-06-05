package com.novoda.downloadmanager;

import android.annotation.SuppressLint;

import java.io.File;

class ByteBasedRemainingStorageRequirementRule implements StorageRequirementRule {

    private final StorageCapacityReader storageCapacityReader;
    private final long bytesRemainingAfterDownload;

    ByteBasedRemainingStorageRequirementRule(StorageCapacityReader storageCapacityReader, long bytesRemainingAfterDownload) {
        this.storageCapacityReader = storageCapacityReader;
        this.bytesRemainingAfterDownload = bytesRemainingAfterDownload;
    }

    @SuppressLint("UsableSpace")
    @Override
    public boolean hasViolatedRule(File storageDirectory,
                                   FileSize downloadFileSize) {
        long storageCapacityInBytes = storageCapacityReader.storageCapacityInBytes(storageDirectory.getPath());
        long usableStorageInBytes = storageDirectory.getUsableSpace();
        long remainingStorageAfterDownloadInBytes = usableStorageInBytes - downloadFileSize.remainingSize();

        Logger.v("Storage capacity in bytes: ", storageCapacityInBytes);
        Logger.v("Usable storage in bytes: ", usableStorageInBytes);
        Logger.v("Minimum required storage in bytes: ", bytesRemainingAfterDownload);
        return remainingStorageAfterDownloadInBytes < bytesRemainingAfterDownload;
    }

}
