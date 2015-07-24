package com.novoda.downloadmanager.lib;

/**
 * Lists the states that the download manager can set on a download
 * to notify applications of the download progress.
 * The codes follow the HTTP families:<br>
 * 1xx: informational<br>
 * 2xx: success<br>
 * 3xx: redirects (not used by the download manager)<br>
 * 4xx: client errors<br>
 * 5xx: server errors
 */
final class DownloadStatus {
    /**
     * This download cannot proceed because the client does not allow it yet
     */
    public static final int QUEUED_DUE_CLIENT_RESTRICTIONS = 187;
    /**
     * This download has been marked for deletion and it will be deleted in the future
     */
    public static final int DELETING = 188;
    /**
     * This download has been submitted to the download executor but not yet started
     */
    public static final int SUBMITTED = 189;
    /**
     * This download hasn't stated yet
     */
    public static final int PENDING = 190;
    /**
     * This download has started
     */
    public static final int RUNNING = 192;
    /**
     * This download has been paused by the owning app.
     */
    public static final int PAUSED_BY_APP = 193;
    /**
     * This download encountered some network error and is waiting before retrying the request.
     */
    public static final int WAITING_TO_RETRY = 194;
    /**
     * This download is waiting for network connectivity to proceed.
     */
    public static final int WAITING_FOR_NETWORK = 195;
    /**
     * This download exceeded a size limit for mobile networks and is waiting for a Wi-Fi
     * connection to proceed.
     */
    public static final int QUEUED_FOR_WIFI = 196;
    /**
     * This download couldn't be completed due to insufficient storage
     * space.  Typically, this is because the SD card is full.
     */
    public static final int INSUFFICIENT_SPACE_ERROR = 198;
    /**
     * This download couldn't be completed because no external storage
     * device was found.  Typically, this is because the SD card is not
     * mounted.
     */
    public static final int DEVICE_NOT_FOUND_ERROR = 199;
    /**
     * This download has successfully completed.
     * Warning: there might be other status values that indicate success
     * in the future.
     * Use isSucccess() to capture the entire category.
     */
    public static final int SUCCESS = 200;
    /**
     * This request couldn't be parsed. This is also used when processing
     * requests with unknown/unsupported URI schemes.
     */
    public static final int BAD_REQUEST = 400;
    /**
     * This download can't be performed because the content type cannot be
     * handled.
     */
    public static final int NOT_ACCEPTABLE = 406;
    /**
     * This download cannot be performed because the length cannot be
     * determined accurately. This is the code for the HTTP error "Length
     * Required", which is typically used when making requests that require
     * a content length but don't have one, and it is also used in the
     * client when a response is received whose length cannot be determined
     * accurately (therefore making it impossible to know when a download
     * completes).
     */
    public static final int LENGTH_REQUIRED = 411;
    /**
     * This download was interrupted and cannot be resumed.
     * This is the code for the HTTP error "Precondition Failed", and it is
     * also used in situations where the client doesn't have an ETag at all.
     */
    public static final int PRECONDITION_FAILED = 412;
    /**
     * The lowest-valued error status that is not an actual HTTP status code.
     */
    public static final int MIN_ARTIFICIAL_ERROR_STATUS = 488;
    /**
     * The requested destination file already exists.
     */
    public static final int FILE_ALREADY_EXISTS_ERROR = 488;
    /**
     * Some possibly transient error occurred, but we can't resume the download.
     */
    public static final int CANNOT_RESUME = 489;
    /**
     * This download was canceled
     */
    public static final int CANCELED = 490;
    /**
     * This download has completed with an error.
     * Warning: there will be other status values that indicate errors in
     * the future. Use isStatusError() to capture the entire category.
     */
    public static final int UNKNOWN_ERROR = 491;
    /**
     * This download couldn't be completed because of a storage issue.
     * Typically, that's because the filesystem is missing or full.
     * Use the more specific {@link #INSUFFICIENT_SPACE_ERROR}
     * and {@link #DEVICE_NOT_FOUND_ERROR} when appropriate.
     */
    public static final int FILE_ERROR = 492;
    /**
     * This download couldn't be completed because of an HTTP
     * redirect response that the download manager couldn't
     * handle.
     */
    public static final int UNHANDLED_REDIRECT = 493;
    /**
     * This download couldn't be completed because of an
     * unspecified unhandled HTTP code.
     */
    public static final int UNHANDLED_HTTP_CODE = 494;
    /**
     * This download couldn't be completed because of an
     * error receiving or processing data at the HTTP level.
     */
    public static final int HTTP_DATA_ERROR = 495;
    /**
     * This download couldn't be completed because of an
     * HttpException while setting up the request.
     */
    public static final int HTTP_EXCEPTION = 496;
    /**
     * This download couldn't be completed because there were
     * too many redirects.
     */
    public static final int TOO_MANY_REDIRECTS = 497;
    /**
     * This download couldn't be completed because another download in the batch failed.
     */
    public static final int BATCH_FAILED = 498;

    /**
     * Returns whether the status is informational (i.e. 1xx).
     */
    public static boolean isInformational(int status) {
        return (status >= 100 && status < 200);
    }

    /**
     * Returns whether the status is a success (i.e. 2xx).
     */
    public static boolean isSuccess(int status) {
        return (status >= 200 && status < 300);
    }

    /**
     * Returns whether the status is an error (i.e. 4xx or 5xx).
     */
    public static boolean isError(int status) {
        return (status >= 400 && status < 600);
    }

    /**
     * Returns whether the status is a client error (i.e. 4xx).
     */
    public static boolean isClientError(int status) {
        return (status >= 400 && status < 500);
    }

    /**
     * Returns whether the status is a server error (i.e. 5xx).
     */
    public static boolean isServerError(int status) {
        return (status >= 500 && status < 600);
    }

    /**
     * this method determines if a notification should be displayed for a
     * given {@link DownloadContract.Downloads#COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI} value
     *
     * @param visibility the value of {@link DownloadContract.Downloads#COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI}.
     * @return true if the notification should be displayed. false otherwise.
     */
    public static boolean isNotificationToBeDisplayed(int visibility) {
        return visibility == android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED ||
                visibility == android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION;
    }

    /**
     * Returns whether the download has completed (either with success or error).
     */
    public static boolean isCompleted(int status) {
        return isSuccess(status) || (isError(status) && !isCancelled(status));
    }

    /**
     * Returns whether the download has been cancelled.
     */
    public static boolean isCancelled(int status) {
        return status == CANCELED;
    }

    public static boolean isSubmitted(int status) {
        return status == SUBMITTED;
    }

    public static boolean isRunning(int status) {
        return status == RUNNING;
    }

    public static boolean isDeleting(int status) {
        return status == DELETING;
    }

    /**
     * Returns whether the download did not start due to insufficient space
     */
    public static boolean isInsufficientSpace(int finalStatus) {
        return finalStatus == INSUFFICIENT_SPACE_ERROR;
    }

    static String statusToString(int status) {
        switch (status) {
            case PENDING:
                return "PENDING";
            case RUNNING:
                return "RUNNING";
            case PAUSED_BY_APP:
                return "PAUSED_BY_APP";
            case WAITING_TO_RETRY:
                return "WAITING_TO_RETRY";
            case WAITING_FOR_NETWORK:
                return "WAITING_FOR_NETWORK";
            case QUEUED_FOR_WIFI:
                return "QUEUED_FOR_WIFI";
            case INSUFFICIENT_SPACE_ERROR:
                return "INSUFFICIENT_SPACE_ERROR";
            case DEVICE_NOT_FOUND_ERROR:
                return "DEVICE_NOT_FOUND_ERROR";
            case SUCCESS:
                return "SUCCESS";
            case BAD_REQUEST:
                return "BAD_REQUEST";
            case NOT_ACCEPTABLE:
                return "NOT_ACCEPTABLE";
            case LENGTH_REQUIRED:
                return "LENGTH_REQUIRED";
            case PRECONDITION_FAILED:
                return "PRECONDITION_FAILED";
            case FILE_ALREADY_EXISTS_ERROR:
                return "FILE_ALREADY_EXISTS_ERROR";
            case CANNOT_RESUME:
                return "CANNOT_RESUME";
            case CANCELED:
                return "CANCELED";
            case UNKNOWN_ERROR:
                return "UNKNOWN_ERROR";
            case FILE_ERROR:
                return "FILE_ERROR";
            case UNHANDLED_REDIRECT:
                return "UNHANDLED_REDIRECT";
            case UNHANDLED_HTTP_CODE:
                return "UNHANDLED_HTTP_CODE";
            case HTTP_DATA_ERROR:
                return "HTTP_DATA_ERROR";
            case HTTP_EXCEPTION:
                return "HTTP_EXCEPTION";
            case TOO_MANY_REDIRECTS:
                return "TOO_MANY_REDIRECTS";
            default:
                return Integer.toString(status);
        }
    }

    private DownloadStatus() {
        // non-instantiable class
    }
}
