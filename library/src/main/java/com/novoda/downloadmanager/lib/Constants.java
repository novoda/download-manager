package com.novoda.downloadmanager.lib;

/**
 * Contains the internal constants that are used in the download manager.
 * As a general rule, modifying these constants should be done with care.
 */
class Constants {

    /**
     * The column that used to be used for the HTTP method of the request
     */
    public static final String RETRY_AFTER_X_REDIRECT_COUNT = "method";

    /**
     * The column that used to be used to reject system filetypes
     */
    public static final String NO_SYSTEM_FILES = "no_system";

    /**
     * The column that is used for the downloads's ETag
     */
    public static final String ETAG = "etag";

    /**
     * The column that is used for the initiating app's UID
     */
    public static final String UID = "uid";

    /**
     * The column that is used to remember whether the media scanner was invoked
     */
    public static final String MEDIA_SCANNED = "scanned";

    /**
     * The column that is used to count retries
     */
    public static final String FAILED_CONNECTIONS = "numfailed";

    /**
     * The intent that gets sent when the service must wake up for a retry
     */
    public static final String ACTION_RETRY = "android.intent.action.DOWNLOAD_WAKEUP";

    /**
     * the intent that gets sent when cancelling a download via a notification action
     */
    public static final String ACTION_CANCEL = "android.intent.action.DOWNLOAD_CANCEL";

    /**
     * The default base name for downloaded files if we can't get one at the HTTP level
     */
    public static final String DEFAULT_DL_FILENAME = "downloadfile";

    /**
     * The default extension for html files if we can't get one at the HTTP level
     */
    public static final String DEFAULT_DL_HTML_EXTENSION = ".html";

    /**
     * The default extension for text files if we can't get one at the HTTP level
     */
    public static final String DEFAULT_DL_TEXT_EXTENSION = ".txt";

    /**
     * The default extension for binary files if we can't get one at the HTTP level
     */
    public static final String DEFAULT_DL_BINARY_EXTENSION = ".bin";

    /**
     * When a number has to be appended to the filename, this string is used to separate the
     * base filename from the sequence number
     */
    public static final String FILENAME_SEQUENCE_SEPARATOR = "-";

    /**
     * Where we store downloaded files on the external storage
     */
    public static final String DEFAULT_DL_SUBDIR = "/download";

    /**
     * A magic filename that is allowed to exist within the system cache
     */
    public static final String KNOWN_SPURIOUS_FILENAME = "lost+found";

    /**
     * A magic filename that is allowed to exist within the system cache
     */
    public static final String RECOVERY_DIRECTORY = "recovery";

    /**
     * The default user agent used for downloads
     */
    public static final String DEFAULT_USER_AGENT = "AndroidDownloadManager";

    /**
     * The MIME type of special DRM files
     */
    public static final String MIMETYPE_DRM_MESSAGE = "application/vnd.oma.drm.message";

    /**
     * The MIME type of APKs
     */
    public static final String MIMETYPE_APK = "application/vnd.android.package";

    /**
     * The size of a tar block
     */
    public static final int TAR_BLOCK_SIZE = 512;

    /**
     * The default blocking factor used by tar
     */
    private static final int DEFAULT_TAR_BLOCKING_FACTOR = 20;

    /**
     * The size of a tar record
     */
    public static final int TAR_RECORD_SIZE = TAR_BLOCK_SIZE * DEFAULT_TAR_BLOCKING_FACTOR;

    /**
     * The buffer size used to stream the data
     */
    public static final int BUFFER_SIZE = 8 * TAR_BLOCK_SIZE;

    /**
     * The value representing the end of stream when, reading an InputStream
     */
    public static final int NO_BYTES_READ = -1;

    /**
     * The minimum amount of progress that has to be done before the progress bar gets updated
     */
    public static final int MIN_PROGRESS_STEP = BUFFER_SIZE;

    /**
     * The minimum amount of time that has to elapse before the progress bar gets updated, in ms
     */
    public static final long MIN_PROGRESS_TIME = 1500;

    /**
     * The maximum number of rows in the database (FIFO)
     */
    public static final int MAX_DOWNLOADS = 1000;

    /**
     * The number of times that the download manager will retry its network
     * operations when no progress is happening before it gives up.
     */
    public static final int MAX_RETRIES = 5;

    /**
     * The minimum amount of time that the download manager accepts for
     * a Retry-After response header with a parameter in delta-seconds.
     */
    public static final int MIN_RETRY_AFTER = 30; // 30s

    /**
     * The maximum amount of time that the download manager accepts for
     * a Retry-After response header with a parameter in delta-seconds.
     */
    public static final int MAX_RETRY_AFTER = 24 * 60 * 60; // 24h

    /**
     * The maximum number of redirects.
     */
    public static final int MAX_REDIRECTS = 5; // can't be more than 7.
    /**
     * The time between a failure and the first retry after an IOException.
     * Each subsequent retry grows exponentially, doubling each time.
     * The time is in seconds.
     */
    public static final int RETRY_FIRST_DELAY = 30;

    /**
     * The size used to represent an unkown byte size.
     */
    public static final int UNKNOWN_BYTE_SIZE = -1;
}
