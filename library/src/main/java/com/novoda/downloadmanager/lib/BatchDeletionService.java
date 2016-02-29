package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.novoda.notils.string.QueryUtils;
import com.novoda.notils.string.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class BatchDeletionService {

    private final DownloadDeleter downloadDeleter;
    private ContentResolver resolver;
    private final Uri downloadsUri;

    BatchDeletionService(DownloadDeleter downloadDeleter, ContentResolver resolver, DownloadsUriProvider downloadsUriProvider) {
        this.downloadDeleter = downloadDeleter;
        this.resolver = resolver;
        this.downloadsUri = downloadsUriProvider.getAllDownloadsUri();
    }

    public void deleteMarkedBatchesFor(Collection<FileDownloadInfo> downloads) {
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

        Cursor cursor = resolver.query(downloadsUri, projection, selection, selectionArgs, null);

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
        resolver.delete(downloadsUri, where, selectionArgs);
    }

    private static class BatchDeletionException extends RuntimeException {
        public BatchDeletionException() {
            super("Failed to query for batches to delete");
        }
    }
}
