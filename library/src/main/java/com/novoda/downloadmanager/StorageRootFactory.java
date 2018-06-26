package com.novoda.downloadmanager;

import android.content.Context;

public final class StorageRootFactory {

    public static StorageRoot createPrimaryStorageRoot(Context context) {
        return new PrimaryStorageRoot(context);
    }

    public static StorageRoot createMissingStorageRoot() {
        return () -> "";
    }

}
