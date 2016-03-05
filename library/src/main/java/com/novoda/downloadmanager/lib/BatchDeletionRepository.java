package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;

import com.novoda.notils.string.QueryUtils;
import com.novoda.notils.string.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class BatchDeletionRepository {

    private final DownloadDeleter downloadDeleter;
    private final ContentResolver resolver;
    private final DownloadsUriProvider downloadsUriProvider;

    BatchDeletionRepository(DownloadDeleter downloadDeleter, ContentResolver resolver, DownloadsUriProvider downloadsUriProvider) {
        this.downloadDeleter = downloadDeleter;
        this.resolver = resolver;
        this.downloadsUriProvider = downloadsUriProvider;
    }

    void deleteMarkedBatchesFor(Collection<FileDownloadInfo> downloads) {
        List<Long> batchIdsToDelete = findBatchIdsToDelete();
        if (batchIdsToDelete.isEmpty()) {
            return;
        }

        deleteFileAndDownloadsFor(downloads, batchIdsToDelete);
        deleteBatchesFromDatabase(batchIdsToDelete);
    }

    private List<Long> findBatchIdsToDelete() {
        Cursor cursor = queryForBatchesToDelete();

        try {
            return marshallToBatchIds(cursor);
        } finally {
            cursor.close();
        }
    }

    private Cursor queryForBatchesToDelete() {
        String[] projection = {DownloadContract.Batches._ID};
        String selection = DownloadContract.Batches.COLUMN_DELETED + " = ?";
        String[] selectionArgs = {"1"};

        Cursor cursor = resolver.query(downloadsUriProvider.getBatchesUri(), projection, selection, selectionArgs, null);

        if (cursor == null) {
            throw new BatchDeletionException();
        }

        return cursor;
    }

    private List<Long> marshallToBatchIds(Cursor cursor) {
        List<Long> batchIdsToDelete = new ArrayList<>();

        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            batchIdsToDelete.add(id);
        }

        return batchIdsToDelete;
    }

    private void deleteFileAndDownloadsFor(Collection<FileDownloadInfo> downloads, List<Long> batchIdsToDelete) {
        for (FileDownloadInfo download : downloads) {
            if (batchIdsToDelete.contains(download.getBatchId())) {
                downloadDeleter.deleteFileAndDatabaseRow(download);
            }
        }
    }

    private void deleteBatchesFromDatabase(List<Long> batchIdsToDelete) {
        String selectionPlaceholders = QueryUtils.createSelectionPlaceholdersOfSize(batchIdsToDelete.size());
        String where = DownloadContract.Batches._ID + " IN (" + selectionPlaceholders + ")";
        String[] selectionArgs = StringUtils.toStringArray(batchIdsToDelete.toArray());
        resolver.delete(downloadsUriProvider.getBatchesUri(), where, selectionArgs);
    }

    private static class BatchDeletionException extends RuntimeException {
        public BatchDeletionException() {
            super("Failed to query for batches to delete");
        }
    }
}
