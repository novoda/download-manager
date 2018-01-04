package com.novoda.downloadmanager;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {RoomBatch.class, RoomFile.class}, version = 1)
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
        return Room.databaseBuilder(
                context.getApplicationContext(),
                RoomAppDatabase.class,
                "database-litedownloadmanager"
        ).build();
    }

}
