package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.SparseIntArray;

import java.util.Arrays;
import java.util.List;

import static com.novoda.downloadmanager.lib.Downloads.Impl.*;

class BatchStatusRepository {

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

    public BatchStatusRepository(ContentResolver resolver) {
        this.resolver = resolver;
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

    long getBatchSizeInBytes(long batchId) {
        Cursor cursor = null;
        long totalSize = 0;
        try {
            String[] selectionArgs = {String.valueOf(batchId)};
            cursor = resolver.query(ALL_DOWNLOADS_CONTENT_URI,
                    null,
                    COLUMN_BATCH_ID + " = ?",
                    selectionArgs,
                    null);

            int totalBytesColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_TOTAL_BYTES);

            while (cursor.moveToNext()) {
                int individualDownloadSize = cursor.getInt(totalBytesColumnIndex);
                totalSize += individualDownloadSize;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return totalSize;
    }

}
