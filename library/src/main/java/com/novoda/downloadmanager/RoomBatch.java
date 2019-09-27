package com.novoda.downloadmanager;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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

    @ColumnInfo(name = "batch_downloaded_date_time_in_millis")
    public long downloadedDateTimeInMillis;

    @ColumnInfo(name = "notification_seen")
    public boolean notificationSeen;

    @ColumnInfo(name = "storage_root")
    public String storageRoot;
}
