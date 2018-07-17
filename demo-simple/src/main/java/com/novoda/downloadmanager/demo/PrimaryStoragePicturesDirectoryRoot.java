package com.novoda.downloadmanager.demo;

import android.content.Context;

import com.novoda.downloadmanager.StorageRoot;

class PrimaryStoragePicturesDirectoryRoot implements StorageRoot {

    private static final String DOWNLOADS_DIR = "/Pictures";

    private final Context context;

    PrimaryStoragePicturesDirectoryRoot(Context context) {
        this.context = context;
    }

    @Override
    public String path() {
        return context.getApplicationContext().getFilesDir().getAbsolutePath() + DOWNLOADS_DIR;
    }
}
