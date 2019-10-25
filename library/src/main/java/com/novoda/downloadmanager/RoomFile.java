package com.novoda.downloadmanager;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import static androidx.room.ForeignKey.CASCADE;

@SuppressWarnings("checkstyle:visibilitymodifier") // Accessors will add a lot of boilerplate code.
@Entity(
        primaryKeys = {"file_id", "batch_id"},
        foreignKeys = @ForeignKey(entity = RoomBatch.class, parentColumns = "batch_id", childColumns = "batch_id", onDelete = CASCADE),
        indices = {@Index("batch_id")}
)
class RoomFile {

    @NonNull
    @ColumnInfo(name = "file_id")
    String fileId;

    @NonNull
    @ColumnInfo(name = "batch_id")
    String batchId;

    @ColumnInfo(name = "file_path")
    String path;

    @ColumnInfo(name = "total_size")
    long totalSize;

    @ColumnInfo(name = "url")
    String url;
}
