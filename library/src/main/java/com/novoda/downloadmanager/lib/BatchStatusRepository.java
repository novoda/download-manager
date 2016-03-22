package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.novoda.notils.string.StringUtils;

import java.util.Arrays;
import java.util.List;

class BatchStatusRepository {

    private final ContentResolver resolver;
    private final DownloadsUriProvider downloadsUriProvider;
    private final SystemFacade systemFacade;
    private final Uri batchesUri;
    private final Uri downloadsUri;
    private final Statuses statuses = new Statuses();

    BatchStatusRepository(ContentResolver resolver, DownloadsUriProvider downloadsUriProvider, SystemFacade systemFacade) {
        this.resolver = resolver;
        this.downloadsUriProvider = downloadsUriProvider;
        this.systemFacade = systemFacade;
        this.batchesUri = downloadsUriProvider.getBatchesUri();
        this.downloadsUri = downloadsUriProvider.getAllDownloadsUri();
    }

    int getBatchStatus(long batchId) {
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
            throw new BatchRetrievalException(batchId);
        }
        return cursor;
    }

    private int marshallToStatus(Cursor cursor) {
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_STATUS));
    }

    int calculateBatchStatusFromDownloads(long batchId) {
        Cursor cursor = queryForDownloadStatusesByBatch(batchId);

        statuses.clear();

        try {
            marshallInStatuses(cursor);
        } finally {
            cursor.close();
        }


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
            throw new BatchRetrievalException(batchId);
        }
        return cursor;
    }

    private void marshallInStatuses(Cursor cursor) {
        while (cursor.moveToNext()) {
            int statusCode = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContract.Downloads.COLUMN_STATUS));
            statuses.incrementCountFor(statusCode);
        }
    }

    int updateBatchToPendingStatus(@NonNull List<String> batchIds) {
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

    void cancelBatch(long batchId) {
        setBatchItemsCancelled(batchId);
        updateBatchStatus(batchId, DownloadStatus.CANCELED);
    }

    void updateBatchStatus(long batchId, int status) {
        ContentValues values = new ContentValues();
        values.put(DownloadContract.Batches.COLUMN_STATUS, status);
        values.put(DownloadContract.Batches.COLUMN_LAST_MODIFICATION, systemFacade.currentTimeMillis());
        resolver.update(ContentUris.withAppendedId(batchesUri, batchId), values, null, null);
    }

    void setBatchItemsCancelled(long batchId) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.CANCELED);
        resolver.update(downloadsUri, values, DownloadContract.Downloads.COLUMN_BATCH_ID + " = ?", new String[]{String.valueOf(batchId)});
    }

    void setBatchItemsFailed(long batchId, long excludedDownloadId) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.BATCH_FAILED);
        String selection = DownloadContract.Downloads.COLUMN_BATCH_ID + " = ? AND " + DownloadContract.Downloads._ID + " <> ? ";
        String[] selectionArgs = {String.valueOf(batchId), String.valueOf(excludedDownloadId)};
        resolver.update(downloadsUri, values, selection, selectionArgs);
    }
}
