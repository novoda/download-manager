package com.novoda.downloadmanager;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {RoomBatch.class, RoomFile.class}, version = 1)
abstract class RoomAppDatabase extends RoomDatabase {

    private static RoomAppDatabase SINGLE_INSTANCE;

    abstract RoomBatchDao roomBatchDao();

    abstract RoomFileDao roomFileDao();

    static RoomAppDatabase obtainInstance(Context context) {
        if (SINGLE_INSTANCE == null) {
            SINGLE_INSTANCE = newInstance(context);
        }
        return SINGLE_INSTANCE;
    }

    private static RoomAppDatabase newInstance(Context context) {
        return Room.databaseBuilder(
                context.getApplicationContext(),
                RoomAppDatabase.class,
                "database-litedownloadmanager"
        ).build();
    }
}
