package com.novoda.downloadmanager;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices={@Index("batch_id")})
class RoomBatch {

    @PrimaryKey
    @ColumnInfo(name = "batch_id")
    public String id;

    @ColumnInfo(name = "batch_title")
    public String title;

    @ColumnInfo(name = "batch_status")
    public String status;
}
