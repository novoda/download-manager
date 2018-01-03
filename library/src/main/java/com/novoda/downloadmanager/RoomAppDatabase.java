package com.novoda.downloadmanager;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {RoomBatch.class, RoomFile.class}, version = 1)
abstract class RoomAppDatabase extends RoomDatabase {

    private static volatile RoomAppDatabase singleInstance;
    private static final Object LOCK = new Object();

    abstract RoomBatchDao roomBatchDao();

    abstract RoomFileDao roomFileDao();

    static RoomAppDatabase obtainInstance(Context context) {
        RoomAppDatabase database = singleInstance;
        if (database == null) {
            synchronized (LOCK) {
                database = singleInstance;
                if (database == null) {
                    singleInstance = newInstance(context);
                    database = singleInstance;
                }
            }
        }
        return database;
    }

    static RoomAppDatabase newInstance(Context context) {
        return Room.databaseBuilder(
                context.getApplicationContext(),
                RoomAppDatabase.class,
                "database-litedownloadmanager"
        ).build();
    }

}
