package com.novoda.downloadmanager;

import android.content.Context;

public final class StorageRootFactory {

    private StorageRootFactory() {
        // Uses static factory methods.
    }

    public static StorageRoot createPrimaryStorageRoot(Context context) {
        return new PrimaryStorageRoot(context);
    }

    public static StorageRoot createMissingStorageRoot() {
        return () -> "";
    }

}
