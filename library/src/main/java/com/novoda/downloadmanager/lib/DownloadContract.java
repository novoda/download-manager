package com.novoda.downloadmanager.lib;

import android.provider.BaseColumns;

final class DownloadContract {

    private DownloadContract() {
        // non-instantiable class
    }

    static final class Downloads implements BaseColumns {

        public static final String DOWNLOADS_TABLE_NAME = "Downloads";
        /**
         * The name of the column containing the URI of the data being downloaded.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_URI = "uri";
        /**
         * The name of the column containing application-specific data.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read/Write</P>
         */
        public static final String COLUMN_APP_DATA = "entity";
        /**
         * The name of the column containing the flags that indicates whether
         * the initiating application is capable of verifying the integrity of
         * the downloaded file. When this flag is set, the download manager
         * performs downloads and reports success even in some situations where
         * it can't guarantee that the download has completed (e.g. when doing
         * a byte-range request without an ETag, or when it can't determine
         * whether a download fully completed).
         * <P>Type: BOOLEAN</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_NO_INTEGRITY = "no_integrity";
        /**
         * The name of the column containing the filename that the initiating
         * application recommends. When possible, the download manager will attempt
         * to use this filename, or a variation, as the actual name for the file.
         * <P>Type: TEXT</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_FILE_NAME_HINT = "hint";
        /**
         * The name of the column containing the MIME type of the downloaded data.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_MIME_TYPE = "mimetype";
        /**
         * The name of the column containing the flag that controls the destination
         * of the download. See the DESTINATION_* constants for a list of legal values.
         * <P>Type: INTEGER</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_DESTINATION = "destination";
        /**
         * The name of the column containing the current control state  of the download.
         * Applications can write to this to control (pause/resume) the download.
         * the CONTROL_* constants for a list of legal values.
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_CONTROL = "control";
        /**
         * The name of the column containing the current status of the download.
         * Applications can read this to follow the progress of each download. See
         * the STATUS_* constants for a list of legal values.
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_STATUS = "status";
        /**
         * The name of the column containing the date at which some interesting
         * status changed in the download. Stored as a System.currentTimeMillis()
         * value.
         * <P>Type: BIGINT</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_LAST_MODIFICATION = "last_modified_timestamp";
        /**
         * The name of the column containing the component name of the class that
         * will receive notifications associated with the download. The
         * package/class combination is passed to
         * Intent.setClassName(String,String).
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_NOTIFICATION_CLASS = "notificationclass";
        /**
         * If extras are specified when requesting a download they will be provided in the intent that
         * is sent to the specified class and package when a download has finished.
         * <P>Type: TEXT</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_NOTIFICATION_EXTRAS = "notificationextras";
        /**
         * The ID of the batch that the download belongs to
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_BATCH_ID = "batch_id";
        /**
         * The name of the column contain the values of the cookie to be used for
         * the download. This is used directly as the value for the Cookie: HTTP
         * header that gets sent with the request.
         * <P>Type: TEXT</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_COOKIE_DATA = "cookiedata";
        /**
         * The name of the column containing the user agent that the initiating
         * application wants the download manager to use for this download.
         * <P>Type: TEXT</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_USER_AGENT = "useragent";
        /**
         * The name of the column containing the referer (sic) that the initiating
         * application wants the download manager to use for this download.
         * <P>Type: TEXT</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_REFERER = "referer";
        /**
         * The name of the column containing the total size of the file being
         * downloaded.
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_TOTAL_BYTES = "total_bytes";
        /**
         * The name of the column containing the size of the part of the file that
         * has been downloaded so far.
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_CURRENT_BYTES = "current_bytes";
        /**
         * The name of the column where the initiating application can provide the
         * UID of another application that is allowed to access this download. If
         * multiple applications share the same UID, all those applications will be
         * allowed to access this download. This column can be updated after the
         * download is initiated. This requires the permission
         * android.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED.
         * <P>Type: INTEGER</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_OTHER_UID = "otheruid";
        /**
         * The name of the column holding a bitmask of allowed network types.  This is only used for
         * public API downloads.
         * <P>Type: INTEGER</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_ALLOWED_NETWORK_TYPES = "allowed_network_types";
        /**
         * The name of the column indicating whether roaming connections can be used.  This is only
         * used for public API downloads.
         * <P>Type: BOOLEAN</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_ALLOW_ROAMING = "allow_roaming";
        /**
         * The name of the column indicating whether metered connections can be used.  This is only
         * used for public API downloads.
         * <P>Type: BOOLEAN</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_ALLOW_METERED = "allow_metered";
        /**
         * Whether or not this download should be displayed in the system's Downloads UI.  Defaults
         * to true.
         * <P>Type: INTEGER</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI = "is_visible_in_downloads_ui";
        /**
         * If true, the user has confirmed that this download can proceed over the mobile network
         * even though it exceeds the recommended maximum size.
         * <P>Type: BOOLEAN</P>
         */
        public static final String COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT = "bypass_recommended_size_limit";
        /**
         * Set to true if this download is deleted. It is completely removed from the database
         * when MediaProvider database also deletes the metadata asociated with this downloaded file.
         * <P>Type: BOOLEAN</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_DELETED = "deleted";
        /**
         * The URI to the corresponding entry in MediaProvider for this downloaded entry. It is
         * used to delete the entries from MediaProvider database when it is deleted from the
         * downloaded list.
         * <P>Type: TEXT</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_MEDIAPROVIDER_URI = "mediaprovider_uri";
        /**
         * The column that is used to remember whether the media scanner was invoked.
         * It can take the values: null or 0(not scanned), 1(scanned), 2 (not scannable).
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_MEDIA_SCANNED = "scanned";
        /**
         * The column with errorMsg for a failed downloaded.
         * Used only for debugging purposes.
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_ERROR_MSG = "errorMsg";
        /**
         * This column stores the source of the last update to this row.
         * This column is only for internal use.
         * Valid values are indicated by LAST_UPDATESRC_* constants.
         * <P>Type: INT</P>
         */
        public static final String COLUMN_LAST_UPDATESRC = "lastUpdateSrc";
        /**
         * The column that is used to count retries
         */
        public static final String COLUMN_FAILED_CONNECTIONS = "numfailed";
        /**
         * The name of the column containing the filename where the downloaded data
         * was actually stored.
         * <P>Type: TEXT</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_DATA = "_data";
        /**
         * default value for {@link Downloads#COLUMN_LAST_UPDATESRC}.
         * This value is used when this column's value is not relevant.
         */
        public static final int LAST_UPDATESRC_NOT_RELEVANT = 0;
        /**
         * One of the values taken by {@link Downloads#COLUMN_LAST_UPDATESRC}.
         * This value is used when the update is NOT to be relayed to the DownloadService
         * (and thus spare DownloadService from scanning the database when this change occurs)
         */
        public static final int LAST_UPDATESRC_DONT_NOTIFY_DOWNLOADSVC = 1;

        /**
         * For you to store any extra data you want to pass through the download manager.
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_EXTRA_DATA = "extra_data";

        /**
         * The column used to flag whether a download should always attempt a resume
         * if it is paused, regardless of what the server says
         */
        public static final String COLUMN_ALWAYS_RESUME = "always_resume";

        private Downloads() {
            // non-instantiable class
        }

    }

    /**
     * Constants related to batches associated with each download.
     */
    static final class Batches implements BaseColumns {

        public static final String BATCHES_TABLE_NAME = "batches";

        /**
         * The name of the column where the initiating application can provided the
         * title of this batch. The title will be displayed ito the user in the
         * list of batches.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read/Write</P>
         */
        public static final String COLUMN_TITLE = "batch_title";

        /**
         * The name of the column where the initiating application can provide the
         * description of this batch. The description will be displayed to the
         * user in the list of batches.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read/Write</P>
         */
        public static final String COLUMN_DESCRIPTION = "batch_description";

        /**
         * A URL that will be used to show a big picture style notification
         */
        public static final String COLUMN_BIG_PICTURE = "batch_notificationBigPictureResourceId";

        /**
         * The status of the batch.
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_STATUS = "batch_status";

        /**
         * The name of the column containing the flags that controls whether the
         * batch is displayed by the UI. See the {@link NotificationVisibility} constants for
         * a list of legal values.
         * <P>Type: INTEGER</P>
         * <P>Owner can Init/Read/Write</P>
         */
        public static final String COLUMN_VISIBILITY = "visibility";

        /**
         * Set to true if this batch is deleted. Its downloads will also be deleted.
         * <P>Type: BOOLEAN</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_DELETED = "deleted";

        /**
         * For you to store any extra data you want to pass through the download manager.
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_EXTRA_DATA = "extra_data";
        /**
         * The name of the column containing the date at which some interesting
         * status changed in the batch. Stored as a System.currentTimeMillis()
         * value.
         * <P>Type: BIGINT</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_LAST_MODIFICATION = "last_modified_timestamp";

        private Batches() {
            // non-instantiable class
        }
    }

    static final class DownloadsByBatch {

        public static final String VIEW_NAME_DOWNLOADS_BY_BATCH = "DownloadsByBatch";

        private DownloadsByBatch() {
            // non-instantiable class
        }

    }

    static final class BatchesWithSizes {

        public static final String VIEW_NAME_BATCHES_WITH_SIZES = "BatchesWithSizes";

        private BatchesWithSizes() {
            // non-instantiable class
        }

        /**
         * The total size of the batch in bytes.
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_TOTAL_BYTES = "batch_total_bytes";

        /**
         * The current size of the batch in bytes (on device).
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_CURRENT_BYTES = "batch_current_bytes";

    }

    static final class DownloadsWithoutProgress {

        public static final String VIEW_NAME_DOWNLOADS_WITHOUT_PROGRESS = "DownloadsWithoutProgress";

        private DownloadsWithoutProgress() {
            // non-instantiable class
        }
    }

    static final class BatchesWithoutProgress {

        public static final String VIEW_NAME_BATCHES_WITHOUT_PROGRESS = "BatchesWithoutProgress";

        private BatchesWithoutProgress() {
            // non-instantiable class
        }
    }

    /**
     * Constants related to HTTP request headers associated with each download.
     */
    static final class RequestHeaders implements BaseColumns {

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

        private RequestHeaders() {
            // non-instantiable class
        }
    }
}
