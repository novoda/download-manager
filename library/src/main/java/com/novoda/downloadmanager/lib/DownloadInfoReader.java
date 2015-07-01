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
        info.setId(getLong(Downloads.Impl._ID));
        info.setUri(getString(Downloads.Impl.COLUMN_URI));
        info.setScannable(getInt(Downloads.Impl.COLUMN_MEDIA_SCANNED) == 1);
        info.setNoIntegrity(getInt(Downloads.Impl.COLUMN_NO_INTEGRITY) == 1);
        info.setHint(getString(Downloads.Impl.COLUMN_FILE_NAME_HINT));
        info.setFileName(getString(Downloads.Impl._DATA));
        info.setMimeType(getString(Downloads.Impl.COLUMN_MIME_TYPE));
        info.setDestination(getInt(Downloads.Impl.COLUMN_DESTINATION));
        info.setStatus(getInt(Downloads.Impl.COLUMN_STATUS));
        info.setNumFailed(getInt(Downloads.Impl.COLUMN_FAILED_CONNECTIONS));
        int retryRedirect = getInt(Constants.RETRY_AFTER_X_REDIRECT_COUNT);
        info.setRetryAfter(retryRedirect & 0xfffffff);
        info.setLastMod(getLong(Downloads.Impl.COLUMN_LAST_MODIFICATION));
        info.setNotificationClassName(getString(Downloads.Impl.COLUMN_NOTIFICATION_CLASS));
        info.setExtras(getString(Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS));
        info.setCookies(getString(Downloads.Impl.COLUMN_COOKIE_DATA));
        info.setUserAgent(getString(Downloads.Impl.COLUMN_USER_AGENT));
        info.setReferer(getString(Downloads.Impl.COLUMN_REFERER));
        info.setTotalBytes(getLong(Downloads.Impl.COLUMN_TOTAL_BYTES));
        info.setCurrentBytes(getLong(Downloads.Impl.COLUMN_CURRENT_BYTES));
        info.seteTag(getString(Constants.ETAG));
        info.setUid(getInt(Constants.UID));
        info.setMediaScanned(getInt(Constants.MEDIA_SCANNED));
        info.setDeleted(getInt(Downloads.Impl.COLUMN_DELETED) == 1);
        info.setMediaProviderUri(getString(Downloads.Impl.COLUMN_MEDIAPROVIDER_URI));
        info.setAllowedNetworkTypes(getInt(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES));
        info.setAllowRoaming(getInt(Downloads.Impl.COLUMN_ALLOW_ROAMING) != 0);
        info.setAllowMetered(getInt(Downloads.Impl.COLUMN_ALLOW_METERED) != 0);
        info.setBypassRecommendedSizeLimit(getInt(Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT));
        info.setBatchId(getLong(Downloads.Impl.COLUMN_BATCH_ID));

        synchronized (this) {
            info.setControl(getInt(Downloads.Impl.COLUMN_CONTROL));
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

        if (info.getCookies() != null) {
            info.addHeader("Cookie", info.getCookies());
        }
        if (info.getReferer() != null) {
            info.addHeader("Referer", info.getReferer());
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
