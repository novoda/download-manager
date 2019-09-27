package com.novoda.downloadmanager;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {RoomBatch.class, RoomFile.class}, version = 4)
abstract class RoomAppDatabase extends RoomDatabase {

    private static final int VERSION_ONE = 1;
    private static final int VERSION_TWO = 2;
    private static final int VERSION_THREE = 3;
    private static final int VERSION_FOUR = 4;

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
                .addMigrations(new VersionOneToVersionTwoMigration())
                .addMigrations(new VersionTwoToVersionThreeMigration(storageRoot))
                .addMigrations(new VersionThreeToVersionFourMigration())
                .build();
    }

    private static final class VersionOneToVersionTwoMigration extends Migration {

        VersionOneToVersionTwoMigration() {
            super(VERSION_ONE, VERSION_TWO);
        }

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `RoomFileTemp` (`file_id` TEXT NOT NULL, `batch_id` TEXT NOT NULL, "
                                     + "`file_path` TEXT, `total_size` INTEGER NOT NULL, `url` TEXT, `persistence_type` TEXT, "
                                     + "PRIMARY KEY(`file_id`, `batch_id`), FOREIGN KEY(`batch_id`) "
                                     + "REFERENCES `RoomBatch`(`batch_id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("DROP INDEX IF EXISTS `index_RoomFileTemp_batch_id`");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_RoomFileTemp_batch_id` ON `RoomFileTemp` (`batch_id`)");
            database.execSQL("INSERT INTO RoomFileTemp (file_id, batch_id, file_path, total_size, url, persistence_type) "
                                     + "SELECT file_id, batch_id, file_path, total_size, url, persistence_type FROM RoomFile");
            database.execSQL("DROP TABLE IF EXISTS RoomFile");
            database.execSQL("ALTER TABLE RoomFileTemp RENAME TO RoomFile");
        }
    }

    private static final class VersionTwoToVersionThreeMigration extends Migration {

        private final StorageRoot storageRoot;

        VersionTwoToVersionThreeMigration(StorageRoot storageRoot) {
            super(VERSION_TWO, VERSION_THREE);
            this.storageRoot = storageRoot;
        }

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE RoomBatch ADD COLUMN 'storage_root' TEXT DEFAULT '" + storageRoot.path() + "'");
        }
    }

    private static final class VersionThreeToVersionFourMigration extends Migration {

        VersionThreeToVersionFourMigration() {
            super(VERSION_THREE, VERSION_FOUR);
        }

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `RoomFileTemp` (`file_id` TEXT NOT NULL, `batch_id` TEXT NOT NULL, "
                                     + "`file_path` TEXT, `total_size` INTEGER NOT NULL, `url` TEXT, "
                                     + "PRIMARY KEY(`file_id`, `batch_id`), FOREIGN KEY(`batch_id`) "
                                     + "REFERENCES `RoomBatch`(`batch_id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("DROP INDEX IF EXISTS `index_RoomFileTemp_batch_id`");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_RoomFileTemp_batch_id` ON `RoomFileTemp` (`batch_id`)");
            database.execSQL("INSERT INTO RoomFileTemp (file_id, batch_id, file_path, total_size, url) "
                                     + "SELECT file_id, batch_id, file_path, total_size, url FROM RoomFile");
            database.execSQL("DROP TABLE IF EXISTS RoomFile");
            database.execSQL("ALTER TABLE RoomFileTemp RENAME TO RoomFile");
        }
    }

}
