package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class BatchStartingService {
    private final ContentResolver resolver;
    private Uri batchesUri;

    public BatchStartingService(ContentResolver resolver, DownloadsUriProvider downloadsUriProvider) {
        this.resolver = resolver;
        this.batchesUri = downloadsUriProvider.getBatchesUri();
    }

    public boolean isBatchStartingForTheFirstTime(long batchId) {

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

        try {
            int hasStarted = DownloadContract.Batches.BATCH_HAS_NOT_STARTED;

            if (cursor.moveToFirst()) {
                hasStarted = Cursors.getInt(cursor, DownloadContract.Batches.COLUMN_HAS_STARTED);
            }

            return hasStarted != DownloadContract.Batches.BATCH_HAS_STARTED;
        } finally {
            cursor.close();
        }
    }

    public void markMatchAsStarted(long batchId) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Batches.COLUMN_HAS_STARTED, DownloadContract.Batches.BATCH_HAS_STARTED);
        resolver.update(ContentUris.withAppendedId(batchesUri, batchId), values, null, null);
    }
}
