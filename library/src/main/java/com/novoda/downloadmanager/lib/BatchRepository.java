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

class BatchRepository {

    private static final List<Integer> PRIORITISED_STATUSES = Arrays.asList(
            DownloadStatus.CANCELED,
            DownloadStatus.RUNNING,
            DownloadStatus.DELETING,

            // Paused statuses
            DownloadStatus.PAUSED_BY_APP,
            DownloadStatus.WAITING_TO_RETRY,
            DownloadStatus.WAITING_FOR_NETWORK,
            DownloadStatus.QUEUED_FOR_WIFI,

            DownloadStatus.PENDING,
            DownloadStatus.SUCCESS
    );

    private static final int PRIORITISED_STATUSES_SIZE = PRIORITISED_STATUSES.size();

    private static final String[] PROJECT_BATCH_ID = {DownloadContract.Batches._ID};
    private static final String WHERE_DELETED_VALUE_IS = DownloadContract.Batches.COLUMN_DELETED + " = ?";
    private static final String[] MARKED_FOR_DELETION = {"1"};

    private final ContentResolver resolver;
    private final DownloadDeleter downloadDeleter;
    private final DownloadsUriProvider downloadsUriProvider;

    BatchRepository(ContentResolver resolver, DownloadDeleter downloadDeleter, DownloadsUriProvider downloadsUriProvider) {
        this.resolver = resolver;
        this.downloadDeleter = downloadDeleter;
        this.downloadsUriProvider = downloadsUriProvider;
    }

    void updateBatchStatus(long batchId, int status) {
        ContentValues values = new ContentValues();
        values.put(DownloadContract.Batches.COLUMN_STATUS, status);
        resolver.update(downloadsUriProvider.getBatchesUri(), values, DownloadContract.Batches._ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    int getBatchStatus(long batchId) {
        Cursor cursor = null;
        SparseIntArray statusCounts = new SparseIntArray(PRIORITISED_STATUSES_SIZE);
        try {
            String[] selectionArgs = {String.valueOf(batchId)};
            cursor = resolver.query(
                    downloadsUriProvider.getAllDownloadsUri(),
                    null,
                    DownloadContract.Downloads.COLUMN_BATCH_ID + " = ?",
                    selectionArgs,
                    null);

            int statusColumnIndex = cursor.getColumnIndexOrThrow(DownloadContract.Downloads.COLUMN_STATUS);

            while (cursor.moveToNext()) {
                int statusCode = cursor.getInt(statusColumnIndex);

                if (DownloadStatus.isError(statusCode)) {
                    return statusCode;
                }

                int currentStatusCount = statusCounts.get(statusCode);
                statusCounts.put(statusCode, currentStatusCount + 1);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        for (int status : PRIORITISED_STATUSES) {
            if (statusCounts.get(status) > 0) {
                return status;
            }
        }

        return DownloadStatus.UNKNOWN_ERROR;
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
        Cursor batchesCursor = resolver.query(this.downloadsUriProvider.getBatchesUri(), null, null, null, null);
        List<DownloadBatch> batches = new ArrayList<>(batchesCursor.getCount());
        try {
            int idColumn = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches._ID);
            int titleIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_TITLE);
            int descriptionIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_DESCRIPTION);
            int bigPictureUrlIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_BIG_PICTURE);
            int statusIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_STATUS);
            int visibilityIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_VISIBILITY);
            int extraDataIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_EXTRA_DATA);
            int totalBatchSizeIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.BatchesWithSizes.COLUMN_TOTAL_BYTES);
            int currentBatchSizeIndex = batchesCursor.getColumnIndexOrThrow(DownloadContract.BatchesWithSizes.COLUMN_CURRENT_BYTES);

            while (batchesCursor.moveToNext()) {
                long id = batchesCursor.getLong(idColumn);
                String title = batchesCursor.getString(titleIndex);
                String description = batchesCursor.getString(descriptionIndex);
                String bigPictureUrl = batchesCursor.getString(bigPictureUrlIndex);
                int status = batchesCursor.getInt(statusIndex);
                @NotificationVisibility.Value int visibility = batchesCursor.getInt(visibilityIndex);
                String extraData = batchesCursor.getString(extraDataIndex);
                long totalSizeBytes = batchesCursor.getLong(totalBatchSizeIndex);
                long currentSizeBytes = batchesCursor.getLong(currentBatchSizeIndex);
                BatchInfo batchInfo = new BatchInfo(title, description, bigPictureUrl, visibility, extraData);

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
        Cursor batchesCursor = resolver.query(downloadsUriProvider.getBatchesUri(), PROJECT_BATCH_ID, WHERE_DELETED_VALUE_IS, MARKED_FOR_DELETION, null);
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
        resolver.delete(downloadsUriProvider.getBatchesUri(), DownloadContract.Batches._ID + " IN (?)", selectionArgs);
    }

    public Cursor retrieveFor(BatchQuery query) {
        return resolver.query(downloadsUriProvider.getBatchesUri(), null, query.getSelection(), query.getSelectionArguments(), query.getSortOrder());
    }
}
