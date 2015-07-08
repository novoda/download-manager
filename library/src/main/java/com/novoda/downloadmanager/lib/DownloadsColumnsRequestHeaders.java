package com.novoda.downloadmanager.lib;

import android.provider.BaseColumns;

/**
 * Constants related to HTTP request headers associated with each download.
 */
public final class DownloadsColumnsRequestHeaders implements BaseColumns {

    public static final String HEADERS_DB_TABLE = "request_headers";
    public static final String COLUMN_DOWNLOAD_ID = "download_id";
    public static final String COLUMN_HEADER = "header";
    public static final String COLUMN_VALUE = "value";

    /**
     * Path segment to add to a download URI to retrieve request headers
     */
    public static final String URI_SEGMENT = "headers";

    /**
     * Prefix for ContentValues keys that contain HTTP header lines, to be passed to
     * DownloadProvider.insert().
     */
    public static final String INSERT_KEY_PREFIX = "http_header_";

    private DownloadsColumnsRequestHeaders() {
        // non-instantiable class
    }
}
