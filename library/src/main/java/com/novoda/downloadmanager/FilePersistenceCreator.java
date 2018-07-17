package com.novoda.downloadmanager;

import android.content.Context;

final class FilePersistenceCreator {

    private final Context context;

    private StorageRequirementRule storageRequirementRule;

    FilePersistenceCreator(Context context) {
        this.context = context.getApplicationContext();
    }

    void withStorageRequirementRules(StorageRequirementRule storageRequirementRule) {
        this.storageRequirementRule = storageRequirementRule;
    }

    FilePersistence create() {
        FilePersistence filePersistence = new PathBasedFilePersistence();
        filePersistence.initialiseWith(context, storageRequirementRule);
        return filePersistence;
    }

}
