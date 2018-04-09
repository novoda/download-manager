package com.novoda.downloadmanager;

import android.os.Build;
import android.os.StatFs;

class StorageCapacityReader {

    long storageCapacityInBytes(StatFs statFs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return statFs.getTotalBytes();
        } else {
            return (long) statFs.getBlockCount() * (long) statFs.getBlockSize();
        }
    }

}
