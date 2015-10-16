package com.novoda.downloadmanager.lib;

import android.content.ContentUris;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import java.io.File;

/**
 * This class wraps a cursor returned by DownloadProvider -- the "underlying cursor" -- and
 * presents a different set of columns, those defined in the DownloadManager.COLUMN_* constants.
 * Some columns correspond directly to underlying values while others are computed from
 * underlying data.
 */
class CursorTranslator extends CursorWrapper {
    private final Uri baseUri;
    private final PublicFacingStatusTranslator statusTranslator;

    public CursorTranslator(Cursor cursor, Uri baseUri, PublicFacingStatusTranslator statusTranslator) {
        super(cursor);
        this.baseUri = baseUri;
        this.statusTranslator = statusTranslator;
    }

    @Override
    public int getInt(int columnIndex) {
        return (int) getLong(columnIndex);
    }

    @Override
    public long getLong(int columnIndex) {
        String columnName = getColumnName(columnIndex);
        switch (columnName) {
            case DownloadManager.COLUMN_REASON:
                return getReason(super.getInt(getColumnIndex(DownloadContract.Downloads.COLUMN_STATUS)));
            case DownloadManager.COLUMN_STATUS:
                return getStatus(super.getInt(getColumnIndex(DownloadContract.Downloads.COLUMN_STATUS)));
            case DownloadManager.COLUMN_BATCH_STATUS:
                return getStatus(super.getInt(getColumnIndex(DownloadContract.Batches.COLUMN_STATUS)));
            default:
                return super.getLong(columnIndex);
        }
    }

    private long getReason(int status) {
        switch (getStatus(status)) {
            case DownloadManager.STATUS_FAILED:
                return getErrorCode(status);

            case DownloadManager.STATUS_PAUSED:
                return getPausedReason(status);

            default:
                return 0; // arbitrary value when status is not an error
        }
    }

    private int getStatus(int rawStatus) {
        return statusTranslator.translate(rawStatus);
    }

    @Override
    public String getString(int columnIndex) {
        return getColumnName(columnIndex).equals(DownloadManager.COLUMN_LOCAL_URI) ? getLocalUri() : super.getString(columnIndex);
    }

    private String getLocalUri() {
        long destinationType = getLong(getColumnIndex(DownloadContract.Downloads.COLUMN_DESTINATION));
        if (destinationType == DownloadsDestination.DESTINATION_FILE_URI
                || destinationType == DownloadsDestination.DESTINATION_EXTERNAL
                || destinationType == DownloadsDestination.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD) {
            String localPath = getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
            if (localPath == null) {
                return null;
            }
            return Uri.fromFile(new File(localPath)).toString();
        }

        // return content URI for cache download
        long downloadId = getLong(getColumnIndex(DownloadContract.Downloads._ID));
        return ContentUris.withAppendedId(baseUri, downloadId).toString();
    }

    private long getPausedReason(int status) {
        switch (status) {
            case DownloadStatus.WAITING_TO_RETRY:
                return DownloadManager.PAUSED_WAITING_TO_RETRY;

            case DownloadStatus.WAITING_FOR_NETWORK:
                return DownloadManager.PAUSED_WAITING_FOR_NETWORK;

            case DownloadStatus.QUEUED_FOR_WIFI:
                return DownloadManager.PAUSED_QUEUED_FOR_WIFI;

            case DownloadStatus.QUEUED_DUE_CLIENT_RESTRICTIONS:
                return DownloadManager.PAUSED_QUEUED_DUE_CLIENT_RESTRICTIONS;

            default:
                return DownloadManager.PAUSED_UNKNOWN;
        }
    }

    private long getErrorCode(int status) {
        if (isHttpClientError(status) || isHttpServerError(status)) {
            // HTTP status code
            return status;
        }

        switch (status) {
            case DownloadStatus.FILE_ERROR:
                return DownloadManager.ERROR_FILE_ERROR;

            case DownloadStatus.UNHANDLED_HTTP_CODE:
            case DownloadStatus.UNHANDLED_REDIRECT:
                return DownloadManager.ERROR_UNHANDLED_HTTP_CODE;

            case DownloadStatus.HTTP_DATA_ERROR:
                return DownloadManager.ERROR_HTTP_DATA_ERROR;

            case DownloadStatus.TOO_MANY_REDIRECTS:
                return DownloadManager.ERROR_TOO_MANY_REDIRECTS;

            case DownloadStatus.INSUFFICIENT_SPACE_ERROR:
                return DownloadManager.ERROR_INSUFFICIENT_SPACE;

            case DownloadStatus.DEVICE_NOT_FOUND_ERROR:
                return DownloadManager.ERROR_DEVICE_NOT_FOUND;

            case DownloadStatus.CANNOT_RESUME:
                return DownloadManager.ERROR_CANNOT_RESUME;

            case DownloadStatus.FILE_ALREADY_EXISTS_ERROR:
                return DownloadManager.ERROR_FILE_ALREADY_EXISTS;

            default:
                return DownloadManager.ERROR_UNKNOWN;
        }
    }

    private boolean isHttpClientError(int status) {
        return 400 <= status && status < DownloadStatus.MIN_ARTIFICIAL_ERROR_STATUS;
    }

    private boolean isHttpServerError(int status) {
        return 500 <= status && status < 600;
    }

}
