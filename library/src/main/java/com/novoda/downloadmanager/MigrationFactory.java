package com.novoda.downloadmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public final class MigrationFactory {

    private MigrationFactory() {
        // Uses static methods.
    }

    public static Migrator createVersionOneToVersionTwoMigrator(Context context, File databasePath, Migrator.Callback callback) {
        if (!databasePath.exists()) {
            return Migrator.NO_OP;
        }

        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(databasePath.getAbsolutePath(), null, 0);
        SqlDatabaseWrapper database = new SqlDatabaseWrapper(sqLiteDatabase);

        MigrationExtractor migrationExtractor = new MigrationExtractor(database);
        RoomDownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(context);
        InternalFilePersistence internalFilePersistence = new InternalFilePersistence();
        internalFilePersistence.initialiseWith(context);
        RoomAppDatabase roomAppDatabase = RoomAppDatabase.obtainInstance(context);
        UnlinkedDataRemover remover = new UnlinkedDataRemover(new AndroidLocalFileDirectory(context), new RoomV2DatabaseFiles(roomAppDatabase.roomFileDao()));
        return new VersionOneToVersionTwoMigrator(migrationExtractor, downloadsPersistence, internalFilePersistence, database, callback, remover);
    }

}
