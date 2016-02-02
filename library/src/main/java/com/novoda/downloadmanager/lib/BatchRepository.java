package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import com.novoda.downloadmanager.notifications.NotificationVisibility;
import com.novoda.notils.string.QueryUtils;
import com.novoda.notils.string.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.novoda.downloadmanager.lib.DownloadContract.Downloads.COLUMN_BATCH_ID;
import static com.novoda.downloadmanager.lib.DownloadContract.Downloads.COLUMN_STATUS;

class BatchRepository {

    private static final List<Integer> PRIORITISED_STATUSES = Arrays.asList(
            DownloadStatus.CANCELED,
            DownloadStatus.PAUSING,
            DownloadStatus.PAUSED_BY_APP,
            DownloadStatus.RUNNING,
            DownloadStatus.DELETING,

            // Paused statuses
            DownloadStatus.QUEUED_DUE_CLIENT_RESTRICTIONS,
            DownloadStatus.WAITING_TO_RETRY,
            DownloadStatus.WAITING_FOR_NETWORK,
            DownloadStatus.QUEUED_FOR_WIFI,

            DownloadStatus.SUBMITTED,
            DownloadStatus.PENDING,
            DownloadStatus.SUCCESS
    );

    private static final List<Integer> STATUSES_EXCEPT_SUCCESS_SUBMITTED = Arrays.asList(
            DownloadStatus.CANCELED,
            DownloadStatus.PAUSED_BY_APP,
            DownloadStatus.RUNNING,
            DownloadStatus.DELETING,

            // Paused statuses
            DownloadStatus.QUEUED_DUE_CLIENT_RESTRICTIONS,
            DownloadStatus.WAITING_TO_RETRY,
            DownloadStatus.WAITING_FOR_NETWORK,
            DownloadStatus.QUEUED_FOR_WIFI,

            DownloadStatus.PENDING
    );

    private static final int PRIORITISED_STATUSES_SIZE = PRIORITISED_STATUSES.size();

    private static final String[] PROJECT_BATCH_ID = {DownloadContract.Batches._ID};
    private static final String WHERE_DELETED_VALUE_IS = DownloadContract.Batches.COLUMN_DELETED + " = ?";
    private static final String[] MARKED_FOR_DELETION = {"1"};

    private final ContentResolver resolver;
    private final DownloadDeleter downloadDeleter;
    private final DownloadsUriProvider downloadsUriProvider;
    private final SystemFacade systemFacade;
    private final StatusCountMap statusCountMap = new StatusCountMap();

    BatchRepository(ContentResolver resolver, DownloadDeleter downloadDeleter, DownloadsUriProvider downloadsUriProvider, SystemFacade systemFacade) {
        this.resolver = resolver;
        this.downloadDeleter = downloadDeleter;
        this.downloadsUriProvider = downloadsUriProvider;
        this.systemFacade = systemFacade;
    }

    void updateBatchStatus(long batchId, int status) {
        ContentValues values = new ContentValues();
        values.put(DownloadContract.Batches.COLUMN_STATUS, status);
        values.put(DownloadContract.Batches.COLUMN_LAST_MODIFICATION, systemFacade.currentTimeMillis());
        resolver.update(downloadsUriProvider.getBatchesUri(), values, DownloadContract.Batches._ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    int getBatchStatus(long batchId) {
        String[] projection = {DownloadContract.Batches.COLUMN_STATUS};
        String where = DownloadContract.Batches._ID + " = ?";
        String[] selectionArgs = {String.valueOf(batchId)};

        Cursor cursor = resolver.query(
                downloadsUriProvider.getBatchesUri(),
                projection,
                where,
                selectionArgs,
                null
        );

        try {
            cursor.moveToFirst();
            return cursor.getInt(0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    int calculateBatchStatus(long batchId) {
        Cursor cursor = null;
        statusCountMap.clear();
        try {
            String[] projection = {DownloadContract.Downloads.COLUMN_STATUS};
            String[] selectionArgs = {String.valueOf(batchId)};

            cursor = resolver.query(
                    downloadsUriProvider.getAllDownloadsUri(),
                    projection,
                    DownloadContract.Downloads.COLUMN_BATCH_ID + " = ?",
                    selectionArgs,
                    null
            );

            while (cursor.moveToNext()) {
                int statusCode = cursor.getInt(0);

                if (DownloadStatus.isError(statusCode)) {
                    return statusCode;
                }

                statusCountMap.increment(statusCode);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        boolean hasCompleteItems = statusCountMap.hasCountFor(DownloadStatus.SUCCESS);
        boolean hasSubmittedItems = statusCountMap.hasCountFor(DownloadStatus.SUBMITTED);
        boolean hasOtherItems = statusCountMap.hasNoItemsWithStatuses(STATUSES_EXCEPT_SUCCESS_SUBMITTED);

        if (hasCompleteItems && hasSubmittedItems && !hasOtherItems) {
            return DownloadStatus.RUNNING;
        }

        for (int status : PRIORITISED_STATUSES) {
            if (statusCountMap.hasCountFor(status)) {
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
        Cursor batchesCursor = resolver.query(
                downloadsUriProvider.getBatchesUri(),
                PROJECT_BATCH_ID,
                WHERE_DELETED_VALUE_IS,
                MARKED_FOR_DELETION,
                null);
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

        String selectionPlaceholders = QueryUtils.createSelectionPlaceholdersOfSize(batchIdsToDelete.size());
        String where = DownloadContract.Batches._ID + " IN (" + selectionPlaceholders + ")";
        String[] selectionArgs = StringUtils.toStringArray(batchIdsToDelete.toArray());
        resolver.delete(downloadsUriProvider.getBatchesUri(), where, selectionArgs);
    }

    public Cursor retrieveFor(BatchQuery query) {
        return resolver.query(downloadsUriProvider.getBatchesUri(), null, query.getSelection(), query.getSelectionArguments(), query.getSortOrder());
    }

    public void setBatchItemsCancelled(long batchId) {
        ContentValues values = new ContentValues(1);
        values.put(COLUMN_STATUS, DownloadStatus.CANCELED);
        resolver.update(downloadsUriProvider.getAllDownloadsUri(), values, COLUMN_BATCH_ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    public void cancelBatch(long batchId) {
        ContentValues downloadValues = new ContentValues(1);
        downloadValues.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.CANCELED);
        resolver.update(
                downloadsUriProvider.getAllDownloadsUri(),
                downloadValues,
                DownloadContract.Downloads.COLUMN_BATCH_ID + " = ?",
                new String[]{String.valueOf(batchId)}
        );
        ContentValues batchValues = new ContentValues(1);
        batchValues.put(DownloadContract.Batches.COLUMN_STATUS, DownloadStatus.CANCELED);
        resolver.update(
                ContentUris.withAppendedId(downloadsUriProvider.getBatchesUri(), batchId),
                batchValues,
                null,
                null
        );
    }

    public void setBatchItemsFailed(long batchId, long downloadId) {
        ContentValues values = new ContentValues(1);
        values.put(COLUMN_STATUS, DownloadStatus.BATCH_FAILED);
        resolver.update(
                downloadsUriProvider.getAllDownloadsUri(),
                values,
                COLUMN_BATCH_ID + " = ? AND " + DownloadContract.Downloads._ID + " <> ? ",
                new String[]{String.valueOf(batchId), String.valueOf(downloadId)}
        );
    }

    /**
     * @return Number of rows updated
     */
    int updateBatchToPendingStatus(@NonNull List<String> batchIdsToBeUnlocked) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Batches.COLUMN_STATUS, DownloadStatus.PENDING);

        int batchIdsSize = batchIdsToBeUnlocked.size();
        String[] whereArray = new String[batchIdsSize];
        String[] selectionArgs = new String[batchIdsSize];

        for (int i = 0; i < batchIdsSize; i++) {
            whereArray[i] = DownloadContract.Batches._ID + " = ?";
            selectionArgs[i] = batchIdsToBeUnlocked.get(i);
        }

        String where = StringUtils.join(Arrays.asList(whereArray), " or ");

        return resolver.update(downloadsUriProvider.getBatchesUri(), values, where, selectionArgs);
    }

    private static class StatusCountMap {

        private final SparseArrayCompat<Integer> statusCounts = new SparseArrayCompat<>(PRIORITISED_STATUSES_SIZE);

        public boolean hasNoItemsWithStatuses(List<Integer> excludedStatuses) {
            boolean hasOtherItems = false;
            for (int status : excludedStatuses) {
                boolean hasItemsForStatus = hasCountFor(status);
                if (hasItemsForStatus) {
                    hasOtherItems = true;
                    break;
                }
            }
            return hasOtherItems;
        }

        public boolean hasCountFor(int status) {
            return statusCounts.get(status, 0) > 0;
        }

        public void increment(int statusCode) {
            int currentStatusCount = statusCounts.get(statusCode, 0);
            statusCounts.put(statusCode, currentStatusCount + 1);
        }

        public void clear() {
            statusCounts.clear();
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("{");

            int size = statusCounts.size();
            for (int i = 0; i < size; i++) {
                stringBuilder.append("[status: ").append(statusCounts.keyAt(i)).append(", count: ").append(statusCounts.valueAt(i)).append("]");
            }

            return stringBuilder.append("}").toString();
        }
    }

}
