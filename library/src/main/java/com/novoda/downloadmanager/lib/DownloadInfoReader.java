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
        info.mId = getLong(Downloads.Impl._ID);
        info.mUri = getString(Downloads.Impl.COLUMN_URI);
        info.mScannable = getInt(Downloads.Impl.COLUMN_MEDIA_SCANNED) == 1;
        info.mNoIntegrity = getInt(Downloads.Impl.COLUMN_NO_INTEGRITY) == 1;
        info.mHint = getString(Downloads.Impl.COLUMN_FILE_NAME_HINT);
        info.mFileName = getString(Downloads.Impl._DATA);
        info.mMimeType = getString(Downloads.Impl.COLUMN_MIME_TYPE);
        info.mDestination = getInt(Downloads.Impl.COLUMN_DESTINATION);
        info.mStatus = getInt(Downloads.Impl.COLUMN_STATUS);
        info.mNumFailed = getInt(Downloads.Impl.COLUMN_FAILED_CONNECTIONS);
        int retryRedirect = getInt(Constants.RETRY_AFTER_X_REDIRECT_COUNT);
        info.mRetryAfter = retryRedirect & 0xfffffff;
        info.mLastMod = getLong(Downloads.Impl.COLUMN_LAST_MODIFICATION);
        info.mClass = getString(Downloads.Impl.COLUMN_NOTIFICATION_CLASS);
        info.mExtras = getString(Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS);
        info.mCookies = getString(Downloads.Impl.COLUMN_COOKIE_DATA);
        info.mUserAgent = getString(Downloads.Impl.COLUMN_USER_AGENT);
        info.mReferer = getString(Downloads.Impl.COLUMN_REFERER);
        info.mTotalBytes = getLong(Downloads.Impl.COLUMN_TOTAL_BYTES);
        info.mCurrentBytes = getLong(Downloads.Impl.COLUMN_CURRENT_BYTES);
        info.mETag = getString(Constants.ETAG);
        info.mUid = getInt(Constants.UID);
        info.mMediaScanned = getInt(Constants.MEDIA_SCANNED);
        info.mDeleted = getInt(Downloads.Impl.COLUMN_DELETED) == 1;
        info.mMediaProviderUri = getString(Downloads.Impl.COLUMN_MEDIAPROVIDER_URI);
        info.mAllowedNetworkTypes = getInt(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES);
        info.mAllowRoaming = getInt(Downloads.Impl.COLUMN_ALLOW_ROAMING) != 0;
        info.mAllowMetered = getInt(Downloads.Impl.COLUMN_ALLOW_METERED) != 0;
        info.mBypassRecommendedSizeLimit = getInt(Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT);
        info.batchId = getLong(Downloads.Impl.COLUMN_BATCH_ID);

        synchronized (this) {
            info.mControl = getInt(Downloads.Impl.COLUMN_CONTROL);
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
                info.addHeader( cursor.getString(headerIndex), cursor.getString(valueIndex));
            }
        } finally {
            cursor.close();
        }

        if (info.mCookies != null) {
            info.addHeader( "Cookie", info.mCookies);
        }
        if (info.mReferer != null) {
            info.addHeader("Referer", info.mReferer);
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
