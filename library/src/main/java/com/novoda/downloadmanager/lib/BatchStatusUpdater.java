package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.SparseIntArray;

import java.util.Arrays;
import java.util.List;

import static com.novoda.downloadmanager.lib.Downloads.Impl.*;

class BatchStatusUpdater {

    private static final List<Integer> PRIORITISED_STATUSES = Arrays.asList(
            // Failed
            STATUS_INSUFFICIENT_SPACE_ERROR,
            STATUS_DEVICE_NOT_FOUND_ERROR,
            STATUS_BAD_REQUEST,
            STATUS_NOT_ACCEPTABLE,
            STATUS_LENGTH_REQUIRED,
            STATUS_PRECONDITION_FAILED,
            STATUS_FILE_ALREADY_EXISTS_ERROR,
            STATUS_CANNOT_RESUME,
            STATUS_UNKNOWN_ERROR,
            STATUS_FILE_ERROR,
            STATUS_UNHANDLED_REDIRECT,
            STATUS_UNHANDLED_HTTP_CODE,
            STATUS_HTTP_DATA_ERROR,
            STATUS_HTTP_EXCEPTION,
            STATUS_TOO_MANY_REDIRECTS,

            // Cancelled
            STATUS_CANCELED,

            // Running
            STATUS_RUNNING,

            // Paused
            STATUS_PAUSED_BY_APP,
            STATUS_WAITING_TO_RETRY,
            STATUS_WAITING_FOR_NETWORK,
            STATUS_QUEUED_FOR_WIFI,

            // Pending
            STATUS_PENDING,

            // Success
            STATUS_SUCCESS
    );

    private final ContentResolver resolver;

    public BatchStatusUpdater(ContentResolver resolver) {
        this.resolver = resolver;
    }

    void updateBatchStatus(long batchId, int status) {
        ContentValues values = new ContentValues();
        values.put(Batches.COLUMN_STATUS, status);
        resolver.update(BATCH_CONTENT_URI, values, Batches._ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    int getBatchStatus(long batchId) {
        Cursor cursor = null;
        try {
            String[] selectionArgs = {String.valueOf(batchId)};
            cursor = resolver.query(ALL_DOWNLOADS_CONTENT_URI,
                    null,
                    COLUMN_BATCH_ID + " = ?",
                    selectionArgs,
                    null);

            int statusColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_STATUS);

            SparseIntArray statusCounts = new SparseIntArray();

            while (cursor.moveToNext()) {
                int status = cursor.getInt(statusColumnIndex);
                statusCounts.put(status, statusCounts.get(status) + 1);
            }

            for (Integer status : PRIORITISED_STATUSES) {
                if (statusCounts.get(status) > 0) {
                    return status;
                }
            }

            return STATUS_UNKNOWN_ERROR;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
