package com.novoda.downloadmanager;

import android.content.Context;

public final class LocalFilesDirectoryFactory {
    private LocalFilesDirectoryFactory() {
        // only has static methods
    }

    public static LocalFilesDirectory create(Context applicationContext) {
        return new AndroidLocalFilesDirectory(applicationContext);
    }
}
