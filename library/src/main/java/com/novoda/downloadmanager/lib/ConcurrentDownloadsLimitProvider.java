package com.novoda.downloadmanager.lib;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.novoda.notils.logger.simple.Log;

class ConcurrentDownloadsLimitProvider {

    private static final int DEFAULT_MAX_CONCURRENT_DOWNLOADS = 5;
    private static final String METADATA_MAX_CONCURRENT_DOWNLOADS = "com.novoda.downloadmanager.MaxConcurrentDownloads";

    private final PackageManager packageManager;
    private final String packageName;

    ConcurrentDownloadsLimitProvider(PackageManager packageManager, String packageName) {
        this.packageManager = packageManager;
        this.packageName = packageName;
    }

    public int getConcurrentDownloadsLimit() {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return getMaximumConcurrentDownloads(applicationInfo.metaData);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Application info not found for: " + packageName + " " + e.getMessage());
            return DEFAULT_MAX_CONCURRENT_DOWNLOADS;
        }
    }

    private int getMaximumConcurrentDownloads(Bundle bundle) {
        if (bundle == null) {
            return DEFAULT_MAX_CONCURRENT_DOWNLOADS;
        }
        return bundle.getInt(METADATA_MAX_CONCURRENT_DOWNLOADS, DEFAULT_MAX_CONCURRENT_DOWNLOADS);
    }

}
