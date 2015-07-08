package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.SparseIntArray;

import com.novoda.notils.string.StringUtils;

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

    private static final String[] PROJECT_BATCH_ID = {Batches._ID};
    private static final String WHERE_DELETED_VALUE_IS = Batches.COLUMN_DELETED + " = ?";
    private static final String[] MARKED_FOR_DELETION = {"1"};

    private final ContentResolver resolver;
    private final DownloadDeleter downloadDeleter;
    private final Downloads downloads;

    BatchRepository(ContentResolver resolver, DownloadDeleter downloadDeleter, Downloads downloads) {
        this.resolver = resolver;
        this.downloadDeleter = downloadDeleter;
        this.downloads = downloads;
    }

    void updateTotalSize(long batchId) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(Batches.COLUMN_TOTAL_BYTES, getSummedBatchSizeInBytes(batchId, COLUMN_TOTAL_BYTES));
        resolver.update(downloads.getBatchContentUri(), updateValues, Batches._ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    void updateCurrentSize(long batchId) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(Batches.COLUMN_CURRENT_BYTES, getSummedBatchSizeInBytes(batchId, COLUMN_CURRENT_BYTES));
        resolver.update(downloads.getBatchContentUri(), updateValues, Batches._ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    private long getSummedBatchSizeInBytes(long batchId, String columnName) {
        Cursor cursor = null;
        long totalSize = 0;
        try {
            String[] selectionArgs = {String.valueOf(batchId)};
            cursor = resolver.query(
                    downloads.getAllDownloadsContentUri(),
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
        resolver.update(downloads.getBatchContentUri(), values, Batches._ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    int getBatchStatus(long batchId) {
        Cursor cursor = null;
        SparseIntArray statusCounts = new SparseIntArray(PRIORITISED_STATUSES_SIZE);
        try {
            String[] selectionArgs = {String.valueOf(batchId)};
            cursor = resolver.query(
                    downloads.getAllDownloadsContentUri(),
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

    public DownloadBatch retrieveBatchFor(FileDownloadInfo download) {
        Collection<FileDownloadInfo> downloads = Collections.singletonList(download);
        List<DownloadBatch> batches = retrieveBatchesFor(downloads);

        for (DownloadBatch batch : batches) {
            if (batch.getBatchId() == download.getBatchId()) {
                return batch;
            }
        }

        return DownloadBatch.DELETED;
    }

    public List<DownloadBatch> retrieveBatchesFor(Collection<FileDownloadInfo> downloads) {
        Cursor batchesCursor = resolver.query(this.downloads.getBatchContentUri(), null, null, null, null);
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

                List<FileDownloadInfo> batchDownloads = new ArrayList<>(1);
                for (FileDownloadInfo fileDownloadInfo : downloads) {
                    if (fileDownloadInfo.getBatchId() == id) {
                        batchDownloads.add(fileDownloadInfo);
                    }
                }
                batches.add(new DownloadBatch(id, batchInfo, batchDownloads, status, totalSizeBytes, currentSizeBytes));
            }
        } finally {
            batchesCursor.close();
        }

        return batches;
    }

    public void deleteMarkedBatchesFor(Collection<FileDownloadInfo> downloads) {
        Cursor batchesCursor = resolver.query(this.downloads.getBatchContentUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
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

    private void deleteBatchesForIds(List<Long> batchIdsToDelete, Collection<FileDownloadInfo> downloads) {
        if (batchIdsToDelete.isEmpty()) {
            return;
        }

        for (FileDownloadInfo download : downloads) {
            if (batchIdsToDelete.contains(download.getBatchId())) {
                downloadDeleter.deleteFileAndDatabaseRow(download);
            }
        }

        String selection = StringUtils.join(batchIdsToDelete, ", ");
        String[] selectionArgs = {selection};
        resolver.delete(this.downloads.getBatchContentUri(), Downloads.Impl.Batches._ID + " IN (?)", selectionArgs);
    }
}
