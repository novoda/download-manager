package com.novoda.downloadmanager;

import android.content.Context;

class PrimaryStorageDownloadsDirectoryRoot implements StorageRoot {

    private static final String DOWNLOADS_DIR = "/downloads";

    private final Context context;

    PrimaryStorageDownloadsDirectoryRoot(Context context) {
        this.context = context;
    }

    @Override
    public String path() {
        return context.getApplicationContext().getFilesDir().getAbsolutePath() + DOWNLOADS_DIR;
    }
}

