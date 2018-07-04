package com.novoda.downloadmanager;

import android.content.Context;

public final class StorageRootFactory {

    private StorageRootFactory() {
        // Uses static factory methods.
    }

    public static StorageRoot createPrimaryStorageDownloadsDirectoryRoot(Context context) {
        return new PrimaryStorageDownloadsDirectoryRoot(context);
    }

    public static StorageRoot createMissingStorageRoot() {
        return () -> "";
    }

}
