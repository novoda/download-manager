package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.novoda.notils.string.QueryUtils;
import com.novoda.notils.string.StringUtils;

import java.util.ArrayList;
import java.util.List;

class DownloadsRepository {

    private final ContentResolver contentResolver;
    private final DownloadInfoCreator downloadInfoCreator;
    private final DownloadsUriProvider downloadsUriProvider;

    public DownloadsRepository(ContentResolver contentResolver,
                               DownloadInfoCreator downloadInfoCreator,
                               DownloadsUriProvider downloadsUriProvider,
                               FileDownloadInfo.ControlStatus.Reader controlReader) {
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

    interface DownloadInfoCreator {

        DownloadInfoCreator NON_FUNCTIONAL = new NonFunctional();

        FileDownloadInfo create(FileDownloadInfo.Reader reader);

        FileDownloadInfo.ControlStatus create(FileDownloadInfo.ControlStatus.Reader reader, long id);

        class NonFunctional implements DownloadInfoCreator {

            @Override
            public FileDownloadInfo create(FileDownloadInfo.Reader reader) {
                LLog.w("DownloadInfoCreator.NonFunctional.create()");
                return null;
            }

            @Override
            public FileDownloadInfo.ControlStatus create(FileDownloadInfo.ControlStatus.Reader reader, long id) {
                LLog.w("DownloadInfoCreator.NonFunctional.ControlStatus.create()");
                return null;
            }
        }
    }

}
