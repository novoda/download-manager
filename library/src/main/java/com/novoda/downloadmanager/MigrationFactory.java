package com.novoda.downloadmanager;

import android.content.Context;

import java.io.File;

public final class MigrationFactory {

    private MigrationFactory() {
        // Uses static methods.
    }

    public static Migrator createVersionOneToVersionTwoMigrator(Context context, File databasePath, Migrator.Callback callback) {
        MigrationExtractor migrationExtractor = new MigrationExtractor();
        RoomDownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);
        InternalFilePersistence internalFilePersistence = new InternalFilePersistence();
        internalFilePersistence.initialiseWith(context);
        return new VersionOneToVersionTwoMigrator(migrationExtractor, downloadsPersistence, internalFilePersistence, databasePath, callback);
    }
}
