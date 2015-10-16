package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.novoda.downloadmanager.lib.logger.LLog;
import com.novoda.notils.string.QueryUtils;
import com.novoda.notils.string.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.novoda.downloadmanager.lib.Constants.UNKNOWN_BYTE_SIZE;
import static com.novoda.downloadmanager.lib.DownloadContract.Downloads.*;

class DownloadsRepository {

    private static final int TRUE_THIS_IS_CLEARER_NOW = 1;

    private final SystemFacade systemFacade;
    private final ContentResolver contentResolver;
    private final DownloadInfoCreator downloadInfoCreator;
    private final DownloadsUriProvider downloadsUriProvider;

    public DownloadsRepository(SystemFacade systemFacade, ContentResolver contentResolver, DownloadInfoCreator downloadInfoCreator,
                               DownloadsUriProvider downloadsUriProvider) {
        this.systemFacade = systemFacade;
        this.contentResolver = contentResolver;
        this.downloadInfoCreator = downloadInfoCreator;
        this.downloadsUriProvider = downloadsUriProvider;
    }

    public List<FileDownloadInfo> getAllDownloads() {
        Cursor downloadsCursor = contentResolver.query(
                downloadsUriProvider.getAllDownloadsUri(),
                null,
                null,
                null,
                DownloadContract.Batches._ID + " ASC"
        );

        try {
            List<FileDownloadInfo> downloads = new ArrayList<>();
            FileDownloadInfo.Reader reader = new FileDownloadInfo.Reader(contentResolver, downloadsCursor);

            while (downloadsCursor.moveToNext()) {
                downloads.add(downloadInfoCreator.create(reader));
            }

            return downloads;
        } finally {
            downloadsCursor.close();
        }
    }

    public FileDownloadInfo getDownloadFor(long id) {
        Uri uri = ContentUris.withAppendedId(downloadsUriProvider.getAllDownloadsUri(), id);
        Cursor downloadsCursor = contentResolver.query(uri, null, null, null, null);
        try {
            downloadsCursor.moveToFirst();
            FileDownloadInfo.Reader reader = new FileDownloadInfo.Reader(contentResolver, downloadsCursor);
            return downloadInfoCreator.create(reader);
        } finally {
            downloadsCursor.close();
        }
    }

    /**
     * Query and return status of requested download.
     */
    public int getDownloadStatus(long id) {
        final Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(downloadsUriProvider.getAllDownloadsUri(), id),
                new String[]{DownloadContract.Downloads.COLUMN_STATUS}, null, null, null
        );
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                // TODO: increase strictness of value returned for unknown
                // downloads; this is safe default for now.
                return DownloadStatus.PENDING;
            }
        } finally {
            cursor.close();
        }
    }

    public void moveDownloadsStatusTo(List<Long> ids, int status) {
        if (ids.isEmpty()) {
            return;
        }

        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, status);

        String selectionPlaceholders = QueryUtils.createSelectionPlaceholdersOfSize(ids.size());
        String where = DownloadContract.Downloads._ID + " IN (" + selectionPlaceholders + ")";
        String[] selectionArgs = StringUtils.toStringArray(ids.toArray());
        contentResolver.update(downloadsUriProvider.getAllDownloadsUri(), values, where, selectionArgs);
    }

    public void pauseDownloadWithBatchId(long batchId) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Downloads.COLUMN_CONTROL, DownloadsControl.CONTROL_PAUSED);

        String where = DownloadContract.Downloads.COLUMN_BATCH_ID + "= ? AND " + DownloadContract.Downloads.COLUMN_STATUS + " != ?";
        String[] selectionArgs = {String.valueOf(batchId), String.valueOf(DownloadStatus.SUCCESS)};

        contentResolver.update(downloadsUriProvider.getAllDownloadsUri(), values, where, selectionArgs);
    }

    public void resumeDownloadWithBatchId(long batchId) {
        ContentValues values = new ContentValues(2);
        values.put(DownloadContract.Downloads.COLUMN_CONTROL, DownloadsControl.CONTROL_RUN);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.PENDING);

        String where = DownloadContract.Downloads.COLUMN_BATCH_ID + "= ? AND " + DownloadContract.Downloads.COLUMN_STATUS + " != ?";
        String[] selectionArgs = {String.valueOf(batchId), String.valueOf(DownloadStatus.SUCCESS)};
        contentResolver.update(downloadsUriProvider.getAllDownloadsUri(), values, where, selectionArgs);
    }

    public void updateDownload(FileDownloadInfo downloadInfo, String filename, String mimeType, int retryAfter, String requestUri, int finalStatus,
                               String errorMsg, int numFailed) {
        ContentValues values = new ContentValues(8);
        values.put(COLUMN_STATUS, finalStatus);
        values.put(DownloadContract.Downloads.COLUMN_DATA, filename);
        values.put(DownloadContract.Downloads.COLUMN_MIME_TYPE, mimeType);
        values.put(DownloadContract.Downloads.COLUMN_LAST_MODIFICATION, systemFacade.currentTimeMillis());
        values.put(DownloadContract.Downloads.COLUMN_FAILED_CONNECTIONS, numFailed);
        values.put(Constants.RETRY_AFTER_X_REDIRECT_COUNT, retryAfter);

        if (!TextUtils.equals(downloadInfo.getUri(), requestUri)) {
            values.put(DownloadContract.Downloads.COLUMN_URI, requestUri);
        }
        if (DownloadStatus.isCompleted(finalStatus)) {
            values.put(DownloadContract.Downloads.COLUMN_CONTROL, DownloadsControl.CONTROL_RUN);
        }

        // save the error message. could be useful to developers.
        if (!TextUtils.isEmpty(errorMsg)) {
            values.put(DownloadContract.Downloads.COLUMN_ERROR_MSG, errorMsg);
        }
        contentResolver.update(downloadInfo.getAllDownloadsUri(), values, null, null);
    }

    public void setDownloadRunning(FileDownloadInfo downloadInfo) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(COLUMN_STATUS, DownloadStatus.RUNNING);
        contentResolver.update(downloadInfo.getAllDownloadsUri(), contentValues, null, null);
    }

    public void pauseDownloadWithSize(FileDownloadInfo downloadInfo, long currentBytes, long totalBytes) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, DownloadStatus.PAUSED_BY_APP);
        values.put(COLUMN_CURRENT_BYTES, currentBytes);
        values.put(COLUMN_TOTAL_BYTES, totalBytes);
        contentResolver.update(downloadInfo.getAllDownloadsUri(), values, null, null);
    }

    public void updateDownloadEndOfStream(FileDownloadInfo downloadInfo, long currentBytes, long contentLength) {
        ContentValues values = new ContentValues(2);
        values.put(COLUMN_CURRENT_BYTES, currentBytes);
        if (contentLength == UNKNOWN_BYTE_SIZE) {
            values.put(COLUMN_TOTAL_BYTES, currentBytes);
        }
        contentResolver.update(downloadInfo.getAllDownloadsUri(), values, null, null);
    }

    public void updateDatabaseFromHeaders(FileDownloadInfo downloadInfo, String filename, String headerETag, String mimeType, long totalBytes) {
        ContentValues values = new ContentValues(4);
        values.put(DownloadContract.Downloads.COLUMN_DATA, filename);
        if (headerETag != null) {
            values.put(Constants.ETAG, headerETag);
        }
        if (mimeType != null) {
            values.put(DownloadContract.Downloads.COLUMN_MIME_TYPE, mimeType);
        }

        values.put(COLUMN_TOTAL_BYTES, totalBytes);
        contentResolver.update(downloadInfo.getAllDownloadsUri(), values, null, null);
    }

    public void deleteDownload(Uri downloadUri) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Downloads.COLUMN_DELETED, TRUE_THIS_IS_CLEARER_NOW);
        contentResolver.update(downloadUri, values, null, null);
    }

    public void setDownloadSubmitted(FileDownloadInfo info) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.SUBMITTED);
        contentResolver.update(info.getAllDownloadsUri(), contentValues, null, null);
    }

    public List<String> getCurrentDownloadingOrSubmittedBatchIds() {
        String[] projection = {"DISTINCT " + DownloadContract.Downloads.COLUMN_BATCH_ID};
        //Can't pass null as selection argument
        String where = "(" + DownloadContract.Downloads.COLUMN_CONTROL + " is null or "
                + DownloadContract.Downloads.COLUMN_CONTROL + " = ? ) "
                + "AND (" + DownloadContract.Downloads.COLUMN_STATUS + " = ? or " + DownloadContract.Downloads.COLUMN_STATUS + " = ?)) "
                + "GROUP BY (" + DownloadContract.Downloads.COLUMN_BATCH_ID;
        String[] selectionArgs = {
                String.valueOf(DownloadsControl.CONTROL_RUN),
                String.valueOf(DownloadStatus.RUNNING),
                String.valueOf(DownloadStatus.SUBMITTED)
        };

        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    downloadsUriProvider.getAllDownloadsUri(),
                    projection,
                    where,
                    selectionArgs,
                    null
            );

            if (cursor == null || cursor.getCount() == 0) {
                return Collections.emptyList();
            }

            List<String> batchIdList = new ArrayList<>();

            while (cursor.moveToNext()) {
                batchIdList.add(cursor.getString(0));
            }
            return batchIdList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * @return Number of rows updated
     */
    public int updateRunningOrSubmittedDownloadsToPending() {
        ContentValues values = new ContentValues(2);
        values.put(DownloadContract.Downloads.COLUMN_CONTROL, DownloadsControl.CONTROL_RUN);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.PENDING);

        //Can't pass null as selection argument
        String where = "(" + DownloadContract.Downloads.COLUMN_CONTROL + " is null or "
                + DownloadContract.Downloads.COLUMN_CONTROL + " = ? ) "
                + " AND ( " + DownloadContract.Downloads.COLUMN_STATUS + " = ? or "
                + DownloadContract.Downloads.COLUMN_STATUS + " = ? )";

        String[] selectionArgs = {
                String.valueOf(DownloadsControl.CONTROL_RUN),
                String.valueOf(DownloadStatus.RUNNING),
                String.valueOf(DownloadStatus.SUBMITTED)
        };

        return contentResolver.update(downloadsUriProvider.getAllDownloadsUri(), values, where, selectionArgs);
    }

    interface DownloadInfoCreator {

        DownloadInfoCreator NON_FUNCTIONAL = new NonFunctional();

        FileDownloadInfo create(FileDownloadInfo.Reader reader);

        class NonFunctional implements DownloadInfoCreator {

            @Override
            public FileDownloadInfo create(FileDownloadInfo.Reader reader) {
                LLog.w("DownloadInfoCreator.NonFunctional.create()");
                return null;
            }
        }
    }

}
