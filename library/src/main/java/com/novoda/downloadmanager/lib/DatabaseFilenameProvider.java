package com.novoda.downloadmanager.lib;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.novoda.notils.logger.simple.Log;

class DatabaseFilenameProvider {
    private static final String DATABASE_FILENAME = "com.novoda.downloadmanager.DatabaseFilename";
    private final PackageManager packageManager;
    private final String packageName;
    private final String defaultFilename;

    DatabaseFilenameProvider(@NonNull PackageManager packageManager, @NonNull String packageName, @NonNull String defaultFilename) {
        this.packageManager = packageManager;
        this.packageName = packageName;
        this.defaultFilename = defaultFilename;
    }

    public String getDatabaseFilename() {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return getDatabaseFilename(applicationInfo.metaData);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Application info not found for: " + packageName + " " + e.getMessage());
            return defaultFilename;
        }
    }

    private String getDatabaseFilename(Bundle bundle) {
        if (bundle == null) {
            return defaultFilename;
        }
        return bundle.getString(DATABASE_FILENAME, defaultFilename);
    }
}
