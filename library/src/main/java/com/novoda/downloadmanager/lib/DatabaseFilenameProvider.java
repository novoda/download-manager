package com.novoda.downloadmanager.lib;

import android.content.Context;
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

    static DatabaseFilenameProvider newInstance(@NonNull Context context, @NonNull String defaultFilename) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getApplicationContext().getPackageName();
        return new DatabaseFilenameProvider(packageManager, packageName, defaultFilename);
    }

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
