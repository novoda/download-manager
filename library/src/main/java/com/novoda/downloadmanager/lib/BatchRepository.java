package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
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

    private static final String[] PROJECT_BATCH_ID = {Downloads.Impl.Batches._ID};
    private static final String SELECT_DELETED = Downloads.Impl.Batches.COLUMN_DELETED;
    private static final String[] WHERE_MARKED_FOR_DELETION = {"1"};

    private final ContentResolver resolver;
    private final DownloadDeleter downloadDeleter;

    public BatchRepository(ContentResolver resolver, DownloadDeleter downloadDeleter) {
        this.resolver = resolver;
        this.downloadDeleter = downloadDeleter;
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
            cursor = resolver.query(
                    ALL_DOWNLOADS_CONTENT_URI,
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

    public DownloadBatch retrieveBatchFor(DownloadInfo download) {
        Collection<DownloadInfo> downloads = Collections.singletonList(download);
        List<DownloadBatch> batches = retrieveBatchesFor(downloads);
        return batches.isEmpty() ? DownloadBatch.DELETED : batches.get(0);
    }

    public List<DownloadBatch> retrieveBatchesFor(Collection<DownloadInfo> downloads) {
        Cursor batchesCursor = resolver.query(Downloads.Impl.BATCH_CONTENT_URI, null, null, null, null);
        List<DownloadBatch> batches = new ArrayList<>(batchesCursor.getCount());
        try {
            int idColumn = batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches._ID);
            int titleIndex = batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_TITLE);
            int descriptionIndex = batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_DESCRIPTION);
            int bigPictureUrlIndex = batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_BIG_PICTURE);
            int statusIndex = batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_STATUS);
            int visibilityColumn = batchesCursor.getColumnIndexOrThrow(Downloads.Impl.Batches.COLUMN_VISIBILITY);
            int totalBatchSizeIndex = batchesCursor.getColumnIndexOrThrow(Batches.COLUMN_TOTAL_BYTES);
            int currentBatchSizeIndex = batchesCursor.getColumnIndexOrThrow(Batches.COLUMN_CURRENT_BYTES);

            while (batchesCursor.moveToNext()) {
                long id = batchesCursor.getLong(idColumn);
                String title = batchesCursor.getString(titleIndex);
                String description = batchesCursor.getString(descriptionIndex);
                String bigPictureUrl = batchesCursor.getString(bigPictureUrlIndex);
                int status = batchesCursor.getInt(statusIndex);
                @NotificationVisibility.Value int visibility = batchesCursor.getInt(visibilityColumn);
                long totalSizeBytes = batchesCursor.getLong(totalBatchSizeIndex);
                long currentSizeBytes = batchesCursor.getLong(currentBatchSizeIndex);
                BatchInfo batchInfo = new BatchInfo(title, description, bigPictureUrl, visibility);

                List<DownloadInfo> batchDownloads = new ArrayList<>(1);
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

    public void deleteMarkedBatchesFor(Collection<DownloadInfo> downloads) {
        Cursor batchesCursor = resolver.query(Downloads.Impl.BATCH_CONTENT_URI, PROJECT_BATCH_ID, SELECT_DELETED, WHERE_MARKED_FOR_DELETION, null);
        List<Long> batchIdsToDelete = new ArrayList<>();
        try {
            while (batchesCursor.moveToNext()) {
                long id = batchesCursor.getLong(0);
                batchIdsToDelete.add(id);
            }
        } finally {
            batchesCursor.close();
        }

        deleteBatchesForIds(batchIdsToDelete, downloads);
    }

    private void deleteBatchesForIds(List<Long> batchIdsToDelete, Collection<DownloadInfo> downloads) {
        if (batchIdsToDelete.isEmpty()) {
            return;
        }

        for (DownloadInfo download : downloads) {
            if (batchIdsToDelete.contains(download.batchId)) {
                downloadDeleter.deleteFileAndDatabaseRow(download);
            }
        }

        String selection = TextUtils.join(", ", batchIdsToDelete);
        String[] selectionArgs = {selection};
        resolver.delete(Downloads.Impl.BATCH_CONTENT_URI, Downloads.Impl.Batches._ID + " IN (?)", selectionArgs);
    }
}
