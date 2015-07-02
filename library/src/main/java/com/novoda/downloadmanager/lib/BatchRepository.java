package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.novoda.downloadmanager.lib.Downloads.Impl.*;

class BatchRepository {

    private static final List<Integer> PRIORITISED_STATUSES = Arrays.asList(
            STATUS_CANCELED,
            STATUS_RUNNING,

            // Paused statuses
            STATUS_PAUSED_BY_APP,
            STATUS_WAITING_TO_RETRY,
            STATUS_WAITING_FOR_NETWORK,
            STATUS_QUEUED_FOR_WIFI,

            STATUS_PENDING,
            STATUS_SUCCESS
    );

    private static final int PRIORITISED_STATUSES_SIZE = PRIORITISED_STATUSES.size();

    private final ContentResolver resolver;

    public BatchRepository(ContentResolver resolver) {
        this.resolver = resolver;
    }

    void updateTotalSize(long batchId) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(Batches.COLUMN_TOTAL_BYTES, getSummedBatchSizeInBytes(batchId, COLUMN_TOTAL_BYTES));
        resolver.update(BATCH_CONTENT_URI, updateValues, Batches._ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    void updateCurrentSize(long batchId) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(Batches.COLUMN_CURRENT_BYTES, getSummedBatchSizeInBytes(batchId, COLUMN_CURRENT_BYTES));
        resolver.update(BATCH_CONTENT_URI, updateValues, Batches._ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    private long getSummedBatchSizeInBytes(long batchId, String columnName) {
        Cursor cursor = null;
        long totalSize = 0;
        try {
            String[] selectionArgs = {String.valueOf(batchId)};
            cursor = resolver.query(
                    ALL_DOWNLOADS_CONTENT_URI,
                    new String[]{"sum(" + columnName + ")"},
                    COLUMN_BATCH_ID + " = ?",
                    selectionArgs,
                    null);

            cursor.moveToFirst();
            totalSize = cursor.getLong(0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return totalSize;
    }

    void updateBatchStatus(long batchId, int status) {
        ContentValues values = new ContentValues();
        values.put(Batches.COLUMN_STATUS, status);
        resolver.update(BATCH_CONTENT_URI, values, Batches._ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    int getBatchStatus(long batchId) {
        Cursor cursor = null;
        SparseIntArray statusCounts = new SparseIntArray(PRIORITISED_STATUSES_SIZE);
        try {
            String[] selectionArgs = {String.valueOf(batchId)};
            cursor = resolver.query(ALL_DOWNLOADS_CONTENT_URI,
                    null,
                    COLUMN_BATCH_ID + " = ?",
                    selectionArgs,
                    null);

            int statusColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_STATUS);

            while (cursor.moveToNext()) {
                int statusCode = cursor.getInt(statusColumnIndex);

                if (Downloads.Impl.isStatusError(statusCode)) {
                    return statusCode;
                }

                Integer currentStatusCount = statusCounts.get(statusCode);
                statusCounts.put(statusCode, currentStatusCount + 1);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        for (Integer status : PRIORITISED_STATUSES) {
            if (statusCounts.get(status) > 0) {
                return status;
            }
        }

        return STATUS_UNKNOWN_ERROR;
    }

    public DownloadBatch retrieveBatchBy(DownloadInfo downloadInfo) {
        return retrieveBatches(Collections.singletonList(downloadInfo)).get(0);
    }

    List<DownloadBatch> retrieveBatches(Collection<DownloadInfo> downloads) {
        List<DownloadBatch> batches = new ArrayList<>();
        Cursor batchesCursor = resolver.query(Downloads.Impl.BATCH_CONTENT_URI, null, null, null, null);
        batches.clear();
        try {
            int idColumn = batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches._ID);
            int visibilityColumn = batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_VISIBILITY);
            while (batchesCursor.moveToNext()) {
                long id = batchesCursor.getLong(idColumn);

                String title = batchesCursor.getString(batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_TITLE));
                String description = batchesCursor.getString(batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_DESCRIPTION));
                String bigPictureUrl = batchesCursor.getString(batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_BIG_PICTURE));
                int status = batchesCursor.getInt(batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_STATUS));
                long totalSizeBytes = batchesCursor.getLong(batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_TOTAL_BYTES));
                long currentSizeBytes = batchesCursor.getLong(batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_CURRENT_BYTES));
                @NotificationVisibility.Value int visibility = batchesCursor.getInt(visibilityColumn);

                BatchInfo batchInfo = new BatchInfo(title, description, bigPictureUrl, visibility);

                List<DownloadInfo> batchDownloads = new ArrayList<>();
                for (DownloadInfo downloadInfo : downloads) {
                    if (downloadInfo.batchId == id) {
                        batchDownloads.add(downloadInfo);
                    }
                }
                batches.add(new DownloadBatch(id, batchInfo, batchDownloads, status, totalSizeBytes, currentSizeBytes));
            }
        } finally {
            batchesCursor.close();
        }
        return batches;
    }
}
