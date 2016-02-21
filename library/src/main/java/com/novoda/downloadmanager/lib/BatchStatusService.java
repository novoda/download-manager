package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import com.novoda.notils.string.StringUtils;

import java.util.Arrays;
import java.util.List;

class BatchStatusService {

    private final ContentResolver resolver;
    private final DownloadsUriProvider downloadsUriProvider;
    private final SystemFacade systemFacade;
    private final Uri batchesUri;
    private final Uri downloadsUri;
    private final Statuses statuses = new Statuses();

    public BatchStatusService(ContentResolver resolver, DownloadsUriProvider downloadsUriProvider, SystemFacade systemFacade) {
        this.resolver = resolver;
        this.downloadsUriProvider = downloadsUriProvider;
        this.systemFacade = systemFacade;
        this.batchesUri = downloadsUriProvider.getBatchesUri();
        this.downloadsUri = downloadsUriProvider.getAllDownloadsUri();
    }

    public int getBatchStatus(long batchId) {
        Cursor cursor = queryForStatus(batchId);

        try {
            return marshallToStatus(cursor);
        } finally {
            cursor.close();
        }
    }

    private Cursor queryForStatus(long batchId) {
        String[] projection = {DownloadContract.Batches.COLUMN_STATUS};

        Cursor cursor = resolver.query(downloadsUriProvider.getSingleBatchUri(batchId), projection, null, null, null);

        if (cursor == null) {
            throw new RuntimeException("Failed to query batches for batchId = " + batchId);
        }
        return cursor;
    }

    private int marshallToStatus(Cursor cursor) {
        cursor.moveToFirst();
        return Cursors.getInt(cursor, DownloadContract.Batches.COLUMN_STATUS);
    }

    public int calculateBatchStatusFromDownloads(long batchId) {
        Cursor cursor = queryForDownloadStatusesByBatch(batchId);

        statuses.clear();

        marshallInStatuses(cursor);

        cursor.close();

        if (statuses.hasErrorStatus()) {
            return statuses.getFirstErrorStatus();
        }

        if (statuses.hasOnlyCompleteAndSubmittedStatuses()) {
            return DownloadStatus.RUNNING;
        }

        return statuses.getFirstStatusByPriority();
    }

    private Cursor queryForDownloadStatusesByBatch(long batchId) {
        String[] projection = {DownloadContract.Downloads.COLUMN_STATUS};
        String[] selectionArgs = {String.valueOf(batchId)};
        String selection = DownloadContract.Downloads.COLUMN_BATCH_ID + " = ?";
        Cursor cursor = resolver.query(downloadsUriProvider.getAllDownloadsUri(), projection, selection, selectionArgs, null);

        if (cursor == null) {
            throw new RuntimeException("Failed to query downloads for batchId = " + batchId);
        }
        return cursor;
    }

    private void marshallInStatuses(Cursor cursor) {
        while (cursor.moveToNext()) {
            int statusCode = Cursors.getInt(cursor, DownloadContract.Downloads.COLUMN_STATUS);
            statuses.incrementCountFor(statusCode);
        }
    }

    public int updateBatchToPendingStatus(@NonNull List<String> batchIds) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Batches.COLUMN_STATUS, DownloadStatus.PENDING);

        int batchIdsSize = batchIds.size();
        String[] whereArray = new String[batchIdsSize];
        String[] selectionArgs = new String[batchIdsSize];

        for (int i = 0; i < batchIdsSize; i++) {
            whereArray[i] = DownloadContract.Batches._ID + " = ?";
            selectionArgs[i] = batchIds.get(i);
        }

        String where = StringUtils.join(Arrays.asList(whereArray), " or ");

        return resolver.update(batchesUri, values, where, selectionArgs);

    }

    public void cancelBatch(long batchId) {
        setBatchItemsCancelled(batchId);
        updateBatchStatus(batchId, DownloadStatus.CANCELED);
    }

    public void updateBatchStatus(long batchId, int status) {
        ContentValues values = new ContentValues();
        values.put(DownloadContract.Batches.COLUMN_STATUS, status);
        values.put(DownloadContract.Batches.COLUMN_LAST_MODIFICATION, systemFacade.currentTimeMillis());
        resolver.update(ContentUris.withAppendedId(batchesUri, batchId), values, null, null);
    }

    public void setBatchItemsCancelled(long batchId) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.CANCELED);
        resolver.update(downloadsUri, values, DownloadContract.Downloads.COLUMN_BATCH_ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    public void setBatchItemsFailed(long batchId, long excludedDownloadId) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.BATCH_FAILED);
        String selection = DownloadContract.Downloads.COLUMN_BATCH_ID + " = ? AND " + DownloadContract.Downloads._ID + " <> ? ";
        String[] selectionArgs = {String.valueOf(batchId), String.valueOf(excludedDownloadId)};
        resolver.update(downloadsUri, values, selection, selectionArgs);

    }

    private static class Statuses {

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
        private static final int NO_ERROR_STATUS = 0;

        private final SparseArrayCompat<Integer> statusCounts = new SparseArrayCompat<>(PRIORITISED_STATUSES.size());
        private int firstErrorStatus = NO_ERROR_STATUS;

        public boolean hasNoItemsWithStatuses(List<Integer> excludedStatuses) {
            for (int status : excludedStatuses) {
                if (hasCountFor(status)) {
                    return false;
                }
            }

            return true;
        }

        public boolean hasCountFor(int statusCode) {
            return statusCounts.get(statusCode, 0) > 0;
        }

        public void incrementCountFor(int statusCode) {
            if (DownloadStatus.isError(statusCode) && !hasErrorStatus()) {
                firstErrorStatus = statusCode;
            }

            int currentStatusCount = statusCounts.get(statusCode, 0);
            statusCounts.put(statusCode, currentStatusCount + 1);
        }

        public void clear() {
            statusCounts.clear();
            firstErrorStatus = NO_ERROR_STATUS;
        }

        private boolean hasOnlyCompleteAndSubmittedStatuses() {
            boolean hasCompleteItems = hasCountFor(DownloadStatus.SUCCESS);
            boolean hasSubmittedItems = hasCountFor(DownloadStatus.SUBMITTED);
            boolean hasNotOtherItems = hasNoItemsWithStatuses(STATUSES_EXCEPT_SUCCESS_SUBMITTED);

            return hasCompleteItems && hasSubmittedItems && hasNotOtherItems;
        }

        public boolean hasErrorStatus() {
            return firstErrorStatus != NO_ERROR_STATUS;
        }

        public int getFirstErrorStatus() {
            return firstErrorStatus;
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

        private int getFirstStatusByPriority() {
            for (int status : PRIORITISED_STATUSES) {
                if (hasCountFor(status)) {
                    return status;
                }
            }

            return DownloadStatus.UNKNOWN_ERROR;
        }
    }
}
