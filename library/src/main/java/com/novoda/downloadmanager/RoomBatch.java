package com.novoda.downloadmanager;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@SuppressWarnings("checkstyle:visibilitymodifier") // Accessors will add a lot of boilerplate code.
@Entity(indices = {@Index("batch_id")})
class RoomBatch {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "batch_id")
    public String id;

    @ColumnInfo(name = "batch_title")
    public String title;

    @ColumnInfo(name = "batch_status")
    public String status;
}
