package com.novoda.downloadmanager;

import android.os.StatFs;

class StorageCapacityReader {

    long storageCapacityInBytes(String path) {
        StatFs statFs = new StatFs(path);
        return statFs.getTotalBytes();
    }

}
