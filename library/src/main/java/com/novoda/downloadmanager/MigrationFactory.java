package com.novoda.downloadmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import java.io.File;

public final class MigrationFactory {

    private MigrationFactory() {
        // Uses static methods.
    }

    public static MigrationServiceBinder migrationServiceBinder(Context context, final Migrator.Callback migrationCallback) {
        final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        Migrator.Callback mainThreadReportingMigrationCallback = new Migrator.Callback() {
            @Override
            public void onUpdate(final String message) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        migrationCallback.onUpdate(message);
                    }
                });
            }
        };

        return new MigrationServiceBinder(context, mainThreadReportingMigrationCallback);
    }

    static Migrator createVersionOneToVersionTwoMigrator(Context context, File databasePath, Migrator.Callback callback) {
        if (!databasePath.exists()) {
            return Migrator.NO_OP;
        }

        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(databasePath.getAbsolutePath(), null, 0);
        SqlDatabaseWrapper database = new SqlDatabaseWrapper(sqLiteDatabase);

        MigrationExtractor migrationExtractor = new MigrationExtractor(database);
        DownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);
        InternalFilePersistence internalFilePersistence = new InternalFilePersistence();
        internalFilePersistence.initialiseWith(context);
        LocalFilesDirectory localFilesDirectory = new AndroidLocalFilesDirectory(context);
        UnlinkedDataRemover remover = new UnlinkedDataRemover(downloadsPersistence, localFilesDirectory);
        return new VersionOneToVersionTwoMigrator(migrationExtractor, downloadsPersistence, internalFilePersistence, database, callback, remover);
    }

}
