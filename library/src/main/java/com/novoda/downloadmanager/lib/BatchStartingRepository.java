package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

class BatchStartingRepository {
    private final ContentResolver resolver;
    private Uri batchesUri;

    BatchStartingRepository(ContentResolver resolver, DownloadsUriProvider downloadsUriProvider) {
        this.resolver = resolver;
        this.batchesUri = downloadsUriProvider.getBatchesUri();
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
                ContentUris.withAppendedId(batchesUri, batchId),
                projection,
                null,
                null,
                null
        );

        if (cursor == null) {
            throw new RuntimeException("Failed to query for batch with batchId = " + batchId);
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
        resolver.update(ContentUris.withAppendedId(batchesUri, batchId), values, null, null);
    }
}
