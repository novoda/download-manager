package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;

class BatchStartingRepository {
    private final ContentResolver resolver;
    private final DownloadsUriProvider downloadsUriProvider;

    BatchStartingRepository(ContentResolver resolver, DownloadsUriProvider downloadsUriProvider) {
        this.resolver = resolver;
        this.downloadsUriProvider = downloadsUriProvider;
    }

    boolean isBatchStartingForTheFirstTime(long batchId) {
        Cursor cursor = queryForBatch(batchId);

        try {
            return batchHasNotAlreadyStarted(cursor);
        } finally {
            cursor.close();
        }
    }

    private Cursor queryForBatch(long batchId) {
        String[] projection = {DownloadContract.Batches.COLUMN_HAS_STARTED};

        Cursor cursor = resolver.query(
                ContentUris.withAppendedId(downloadsUriProvider.getBatchesUri(), batchId),
                projection,
                null,
                null,
                null
        );

        if (cursor == null) {
            throw BatchQueryException.failedQueryForBatch(batchId);
        }

        return cursor;
    }

    private boolean batchHasNotAlreadyStarted(Cursor cursor) {
        int hasStarted = DownloadContract.Batches.BATCH_HAS_NOT_STARTED;

        if (cursor.moveToFirst()) {
            hasStarted = Cursors.getInt(cursor, DownloadContract.Batches.COLUMN_HAS_STARTED);
        }

        return hasStarted != DownloadContract.Batches.BATCH_HAS_STARTED;
    }

    void markBatchAsStarted(long batchId) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Batches.COLUMN_HAS_STARTED, DownloadContract.Batches.BATCH_HAS_STARTED);
        resolver.update(ContentUris.withAppendedId(downloadsUriProvider.getBatchesUri(), batchId), values, null, null);
    }

    private static class BatchQueryException extends RuntimeException {
        public BatchQueryException(String message) {
            super(message);
        }

        private static BatchQueryException failedQueryForBatch(long batchId) {
            return new BatchQueryException("Failed to query for batch with batchId = " + batchId);
        }

    }
}
