package com.novoda.downloadmanager;

import android.content.Context;

class FilePersistenceCreator {

    private final Context context;

    private StorageRequirementRule storageRequirementRule;

    static FilePersistenceCreator newPathBasedPersistenceCreator(Context context) {
        return new FilePersistenceCreator(context);
    }

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
