package com.novoda.downloadmanager;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {RoomBatch.class, RoomFile.class}, version = 3)
abstract class RoomAppDatabase extends RoomDatabase {

    private static volatile RoomAppDatabase singleInstance;

    abstract RoomBatchDao roomBatchDao();

    abstract RoomFileDao roomFileDao();

    @SuppressWarnings("PMD.NonThreadSafeSingleton")     // See https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java.
    static RoomAppDatabase obtainInstance(Context context) {
        if (singleInstance == null) {
            synchronized (RoomAppDatabase.class) {
                if (singleInstance == null) {
                    singleInstance = newInstance(context);
                }
            }
        }
        return singleInstance;
    }

    static RoomAppDatabase newInstance(Context context) {
        StorageRoot storageRoot = StorageRootFactory.createPrimaryStorageDownloadsDirectoryRoot(context.getApplicationContext());
        return Room.databaseBuilder(
                context.getApplicationContext(),
                RoomAppDatabase.class,
                "database-litedownloadmanager"
        )
                .addMigrations(MIGRATION_V1_TO_V2)
                .addMigrations(new VersionTwoToVersionThreeMigration(storageRoot))
                .build();
    }

    private static final Migration MIGRATION_V1_TO_V2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `RoomFileTemp` (`file_id` TEXT NOT NULL, `batch_id` TEXT NOT NULL, "
                                     + "`file_path` TEXT, `total_size` INTEGER NOT NULL, `url` TEXT, `persistence_type` TEXT, "
                                     + "PRIMARY KEY(`file_id`, `batch_id`), FOREIGN KEY(`batch_id`) "
                                     + "REFERENCES `RoomBatch`(`batch_id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX `index_RoomFileTemp_batch_id` ON `RoomFileTemp` (`batch_id`)");
            database.execSQL("INSERT INTO RoomFileTemp (file_id, batch_id, file_path, total_size, url, persistence_type) "
                                     + "SELECT file_id, batch_id, file_path, total_size, url, persistence_type FROM RoomFile");
            database.execSQL("DROP TABLE RoomFile");
            database.execSQL("ALTER TABLE RoomFileTemp RENAME TO RoomFile");
        }
    };

    private static class VersionTwoToVersionThreeMigration extends Migration {

        private final StorageRoot storageRoot;

        private VersionTwoToVersionThreeMigration(StorageRoot storageRoot) {
            super(2, 3);
            this.storageRoot = storageRoot;
        }

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE RoomBatch ADD COLUMN 'storage_root' TEXT DEFAULT '" + storageRoot.path() + "'");
        }
    }

}
