package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.novoda.downloadmanager.notifications.NotificationVisibility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class BatchRetrievalRepository {
    private final ContentResolver resolver;
    private final DownloadsUriProvider downloadsUriProvider;

    BatchRetrievalRepository(ContentResolver resolver,
                             DownloadsUriProvider downloadsUriProvider) {
        this.resolver = resolver;
        this.downloadsUriProvider = downloadsUriProvider;
    }

    List<DownloadBatch> retrieveBatchesFor(Collection<FileDownloadInfo> downloads) {
        Cursor cursor = queryForAllBatches();

        try {
            return marshallDownloadBatches(downloads, cursor);
        } finally {
            cursor.close();
        }
    }

    private Cursor queryForAllBatches() {
        Uri batchesUri = downloadsUriProvider.getBatchesUri();
        Cursor cursor = resolver.query(batchesUri, null, null, null, null);

        if (cursor == null) {
            throw new BatchRetrievalException();
        }

        return cursor;
    }

    private List<DownloadBatch> marshallDownloadBatches(Collection<FileDownloadInfo> downloads, Cursor batchesCursor) {
        List<DownloadBatch> batches = new ArrayList<>(batchesCursor.getCount());
        while (batchesCursor.moveToNext()) {
            batches.add(marshallDownloadBatch(downloads, batchesCursor));
        }

        return batches;
    }

    DownloadBatch retrieveBatchFor(FileDownloadInfo download) {
        Cursor cursor = queryForSingleBatch(download.getBatchId());

        try {
            if (cursor.moveToFirst()) {
                return marshallDownloadBatch(Collections.singletonList(download), cursor);
            } else {
                return DownloadBatch.DELETED;
            }
        } finally {
            cursor.close();
        }
    }

    private Cursor queryForSingleBatch(long batchId) {
        Uri batchUri = downloadsUriProvider.getSingleBatchUri(batchId);

        Cursor cursor = resolver.query(batchUri, null, null, null, null);

        if (cursor == null) {
            throw new BatchRetrievalException(batchId);
        }

        return cursor;
    }

    private DownloadBatch marshallDownloadBatch(Collection<FileDownloadInfo> downloads, Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadContract.Batches._ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_DESCRIPTION));
        String bigPictureUrl = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_BIG_PICTURE));
        int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_STATUS));
        @NotificationVisibility.Value int visibility = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_VISIBILITY));
        String extraData = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContract.Batches.COLUMN_EXTRA_DATA));
        long totalSizeBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadContract.BatchesWithSizes.COLUMN_TOTAL_BYTES));
        long currentSizeBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadContract.BatchesWithSizes.COLUMN_CURRENT_BYTES));
        BatchInfo batchInfo = new BatchInfo(title, description, bigPictureUrl, visibility, extraData);

        List<FileDownloadInfo> batchDownloads = new ArrayList<>(1);
        for (FileDownloadInfo fileDownloadInfo : downloads) {
            if (fileDownloadInfo.getBatchId() == id) {
                batchDownloads.add(fileDownloadInfo);
            }
        }

        return new DownloadBatch(id, batchInfo, batchDownloads, status, totalSizeBytes, currentSizeBytes);
    }

    Cursor retrieveFor(BatchQuery query) {
        Uri batchesUri = downloadsUriProvider.getBatchesUri();
        return resolver.query(
                batchesUri,
                null,
                query.getSelection(),
                query.getSelectionArguments(),
                query.getSortOrder()
        );
    }

}
