package com.novoda.downloadmanager;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
interface RoomFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomFile roomFile);

    @Transaction
    @Query("SELECT * FROM RoomFile WHERE RoomFile.batch_id = :batchId")
    List<RoomFile> loadAllFilesFor(String batchId);

    @Transaction
    @Query("SELECT * FROM RoomFile")
    List<RoomFile> loadAllFiles();
}
