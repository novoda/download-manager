package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

public class DownloadInfoReader {
    private final ContentResolver resolver;
    private final Cursor cursor;

    public DownloadInfoReader(ContentResolver resolver, Cursor cursor) {
        this.resolver = resolver;
        this.cursor = cursor;
    }

    public DownloadInfo newDownloadInfo(
            Context context,
            SystemFacade systemFacade,
            StorageManager storageManager,
            DownloadNotifier notifier,
            DownloadClientReadyChecker downloadClientReadyChecker) {
        RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator();
        ContentValues contentValues = new ContentValues();
        DownloadInfo info = new DownloadInfo(
                context,
                systemFacade,
                storageManager,
                notifier,
                randomNumberGenerator,
                downloadClientReadyChecker,
                contentValues);
        updateFromDatabase(info);
        readRequestHeaders(info);

        return info;
    }

    public void updateFromDatabase(DownloadInfo info) {
        info.id = getLong(Downloads.Impl._ID);
        info.uri = getString(Downloads.Impl.COLUMN_URI);
        info.scannable = getInt(Downloads.Impl.COLUMN_MEDIA_SCANNED) == 1;
        info.noIntegrity = getInt(Downloads.Impl.COLUMN_NO_INTEGRITY) == 1;
        info.hint = getString(Downloads.Impl.COLUMN_FILE_NAME_HINT);
        info.fileName = getString(Downloads.Impl._DATA);
        info.mimeType = getString(Downloads.Impl.COLUMN_MIME_TYPE);
        info.destination = getInt(Downloads.Impl.COLUMN_DESTINATION);
        info.status = getInt(Downloads.Impl.COLUMN_STATUS);
        info.numFailed = getInt(Downloads.Impl.COLUMN_FAILED_CONNECTIONS);
        int retryRedirect = getInt(Constants.RETRY_AFTER_X_REDIRECT_COUNT);
        info.retryAfter = retryRedirect & 0xfffffff;
        info.lastMod = getLong(Downloads.Impl.COLUMN_LAST_MODIFICATION);
        info.notificationClassName = getString(Downloads.Impl.COLUMN_NOTIFICATION_CLASS);
        info.extras = getString(Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS);
        info.cookies = getString(Downloads.Impl.COLUMN_COOKIE_DATA);
        info.userAgent = getString(Downloads.Impl.COLUMN_USER_AGENT);
        info.referer = getString(Downloads.Impl.COLUMN_REFERER);
        info.totalBytes = getLong(Downloads.Impl.COLUMN_TOTAL_BYTES);
        info.currentBytes = getLong(Downloads.Impl.COLUMN_CURRENT_BYTES);
        info.eTag = getString(Constants.ETAG);
        info.uid = getInt(Constants.UID);
        info.mediaScanned = getInt(Constants.MEDIA_SCANNED);
        info.deleted = getInt(Downloads.Impl.COLUMN_DELETED) == 1;
        info.mediaProviderUri = getString(Downloads.Impl.COLUMN_MEDIAPROVIDER_URI);
        info.allowedNetworkTypes = getInt(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES);
        info.allowRoaming = getInt(Downloads.Impl.COLUMN_ALLOW_ROAMING) != 0;
        info.allowMetered = getInt(Downloads.Impl.COLUMN_ALLOW_METERED) != 0;
        info.bypassRecommendedSizeLimit = getInt(Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT);
        info.batchId = getLong(Downloads.Impl.COLUMN_BATCH_ID);

        synchronized (this) {
            info.control = getInt(Downloads.Impl.COLUMN_CONTROL);
        }
    }

    private void readRequestHeaders(DownloadInfo info) {
        info.clearHeaders();
        Uri headerUri = Uri.withAppendedPath(info.getAllDownloadsUri(), Downloads.Impl.RequestHeaders.URI_SEGMENT);
        Cursor cursor = resolver.query(headerUri, null, null, null, null);
        try {
            int headerIndex =
                    cursor.getColumnIndexOrThrow(Downloads.Impl.RequestHeaders.COLUMN_HEADER);
            int valueIndex =
                    cursor.getColumnIndexOrThrow(Downloads.Impl.RequestHeaders.COLUMN_VALUE);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                info.addHeader(cursor.getString(headerIndex), cursor.getString(valueIndex));
            }
        } finally {
            cursor.close();
        }

        if (info.cookies != null) {
            info.addHeader("Cookie", info.cookies);
        }
        if (info.referer != null) {
            info.addHeader("Referer", info.referer);
        }
    }

    private String getString(String column) {
        int index = cursor.getColumnIndexOrThrow(column);
        String s = cursor.getString(index);
        return (TextUtils.isEmpty(s)) ? null : s;
    }

    private Integer getInt(String column) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(column));
    }

    private Long getLong(String column) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(column));
    }
}
