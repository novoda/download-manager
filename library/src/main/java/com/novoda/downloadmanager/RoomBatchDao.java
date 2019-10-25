package com.novoda.downloadmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
interface RoomBatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomBatch roomBatch);

    @Transaction
    @Query("SELECT * FROM RoomBatch")
    List<RoomBatch> loadAll();

    @Transaction
    @Query("SELECT * FROM RoomBatch WHERE RoomBatch.batch_id = :batchId")
    RoomBatch load(String batchId);

    @Delete
    void delete(RoomBatch... roomBatches);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(RoomBatch... roomBatches);
}
