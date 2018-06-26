package com.novoda.downloadmanager;

import android.content.Context;

class PrimaryStorageRoot implements StorageRoot {

    private final Context context;

    PrimaryStorageRoot(Context context) {
        this.context = context;
    }

    @Override
    public String path() {
        return context.getApplicationContext().getFilesDir().getAbsolutePath();
    }
}

