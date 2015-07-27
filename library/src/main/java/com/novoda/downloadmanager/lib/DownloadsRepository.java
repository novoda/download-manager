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
import java.util.List;

import static com.novoda.downloadmanager.lib.DownloadContract.Downloads.COLUMN_STATUS;

class DownloadsRepository {

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
                DownloadContract.Batches._ID + " ASC");

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
