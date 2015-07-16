package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Stores information about an individual download.
 */
class FileDownloadInfo {

    private static final int UNKNOWN_BYTES = -1;

    // TODO: move towards these in-memory objects being sources of truth, and periodically pushing to provider.

    /**
     * Constants used to indicate network state for a specific download, after
     * applying any requested constraints.
     */
    public enum NetworkState {
        /**
         * The network is usable for the given download.
         */
        OK,

        /**
         * There is no network connectivity.
         */
        NO_CONNECTION,

        /**
         * The download exceeds the maximum size for this network.
         */
        UNUSABLE_DUE_TO_SIZE,

        /**
         * The download exceeds the recommended maximum size for this network,
         * the user must confirm for this download to proceed without WiFi.
         */
        RECOMMENDED_UNUSABLE_DUE_TO_SIZE,

        /**
         * The current connection is roaming, and the download can't proceed
         * over a roaming connection.
         */
        CANNOT_USE_ROAMING,

        /**
         * The app requesting the download specific that it can't use the
         * current network connection.
         */
        TYPE_DISALLOWED_BY_REQUESTOR,

        /**
         * Current network is blocked for requesting application.
         */
        BLOCKED
    }

    private long id;
    private String uri;
    private boolean scannable;
    private boolean noIntegrity;
    private String hint;
    private String fileName;
    private String mimeType;
    private int destination;
    private int control;
    private int status;
    private int numFailed;
    private int retryAfter;
    private long lastMod;
    private String notificationClassName;
    private String extras;
    private String cookies;
    private String userAgent;
    private String referer;
    private long totalBytes;
    private long currentBytes;
    private String eTag;
    private int uid;
    private int mediaScanned;
    private boolean deleted;
    private String mediaProviderUri;
    private int allowedNetworkTypes;
    private boolean allowRoaming;
    private boolean allowMetered;
    private int bypassRecommendedSizeLimit;
    private long batchId;
    private boolean alwaysResume;

    private final List<Pair<String, String>> requestHeaders = new ArrayList<>();
    private final SystemFacade systemFacade;
    private final RandomNumberGenerator randomNumberGenerator;
    private final DownloadsUriProvider downloadsUriProvider;

    FileDownloadInfo(SystemFacade systemFacade, RandomNumberGenerator randomNumberGenerator, DownloadsUriProvider downloadsUriProvider) {
        this.systemFacade = systemFacade;
        this.randomNumberGenerator = randomNumberGenerator;
        this.downloadsUriProvider = downloadsUriProvider;
    }

    public long getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public boolean isNoIntegrity() {
        return noIntegrity;
    }

    public String getHint() {
        return hint;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getDestination() {
        return destination;
    }

    public int getControl() {
        return control;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        // TODO remove me!
        this.status = status;
    }

    public int getNumFailed() {
        return numFailed;
    }

    public String getNotificationClassName() {
        return notificationClassName;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public String getETag() {
        return eTag;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getMediaProviderUri() {
        return mediaProviderUri;
    }

    public long getBatchId() {
        return batchId;
    }

    public boolean allowMetered() {
        return allowMetered;
    }

    public boolean allowRoaming() {
        return allowRoaming;
    }

    public boolean isRecommendedSizeLimitBypassed() {
        return bypassRecommendedSizeLimit == 0;
    }

    public String getExtras() {
        return extras;
    }

    public Collection<Pair<String, String>> getHeaders() {
        return Collections.unmodifiableList(requestHeaders);
    }

    /**
     * Returns the time when a download should be restarted.
     */
    public long restartTime(long now) {
        if (numFailed == 0) {
            return now;
        }
        if (retryAfter > 0) {
            return lastMod + retryAfter;
        }
        return lastMod + Constants.RETRY_FIRST_DELAY * (1000 + randomNumberGenerator.generate()) * (1 << (numFailed - 1));
    }

    /**
     * Translate a ConnectivityManager.TYPE_* constant to the corresponding
     * DownloadManager.Request.NETWORK_* bit flag.
     */
    private int translateNetworkTypeToApiFlag(int networkType) {
        switch (networkType) {
            case ConnectivityManager.TYPE_MOBILE:
                return Request.NETWORK_MOBILE;

            case ConnectivityManager.TYPE_WIFI:
                return Request.NETWORK_WIFI;

            case ConnectivityManager.TYPE_BLUETOOTH:
                return Request.NETWORK_BLUETOOTH;

            default:
                return 0;
        }
    }

    /**
     * Check if the download's size prohibits it from running over the current network.
     *
     * @return one of the NETWORK_* constants
     */
    private NetworkState checkSizeAllowedForNetwork(int networkType) {
        if (totalBytes <= 0) {
            return NetworkState.OK; // we don't know the size yet
        }
        if (networkType == ConnectivityManager.TYPE_WIFI) {
            return NetworkState.OK; // anything goes over wifi
        }
        Long maxBytesOverMobile = systemFacade.getMaxBytesOverMobile();
        if (maxBytesOverMobile != null && totalBytes > maxBytesOverMobile) {
            return NetworkState.UNUSABLE_DUE_TO_SIZE;
        }
        if (bypassRecommendedSizeLimit == 0) {
            Long recommendedMaxBytesOverMobile = systemFacade.getRecommendedMaxBytesOverMobile();
            if (recommendedMaxBytesOverMobile != null
                    && totalBytes > recommendedMaxBytesOverMobile) {
                return NetworkState.RECOMMENDED_UNUSABLE_DUE_TO_SIZE;
            }
        }
        return NetworkState.OK;
    }

    public boolean isSubmittedOrRunning() {
        return DownloadStatus.isSubmitted(status) || DownloadStatus.isRunning(status);
    }

    /**
     * If download is ready to be scanned, enqueue it into the given
     * {@link DownloadScanner}.
     *
     * @return If actively scanning.
     */
    public boolean startScanIfReady(DownloadScanner scanner) {
        synchronized (this) {
            final boolean isReady = shouldScanFile();
            if (isReady) {
                scanner.requestScan(this);
            }
            return isReady;
        }
    }

    public boolean isOnCache() {
        return (destination == DownloadsDestination.DESTINATION_CACHE_PARTITION
                || destination == DownloadsDestination.DESTINATION_SYSTEMCACHE_PARTITION
                || destination == DownloadsDestination.DESTINATION_CACHE_PARTITION_NOROAMING
                || destination == DownloadsDestination.DESTINATION_CACHE_PARTITION_PURGEABLE);
    }

    public Uri getMyDownloadsUri() {
        return ContentUris.withAppendedId(downloadsUriProvider.getContentUri(), id);
    }

    public Uri getAllDownloadsUri() {
        return ContentUris.withAppendedId(downloadsUriProvider.getAllDownloadsUri(), id);
    }

    /**
     * Returns whether a file should be scanned
     */
    private boolean shouldScanFile() {
        return (mediaScanned == 0)
                && (getDestination() == DownloadsDestination.DESTINATION_EXTERNAL ||
                getDestination() == DownloadsDestination.DESTINATION_FILE_URI ||
                getDestination() == DownloadsDestination.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD)
                && DownloadStatus.isSuccess(getStatus())
                && scannable;
    }

    /**
     * Query and return status of requested download.
     */
    public static int queryDownloadStatus(ContentResolver resolver, long id, DownloadsUriProvider downloadsUriProvider) {
        final Cursor cursor = resolver.query(
                ContentUris.withAppendedId(downloadsUriProvider.getAllDownloadsUri(), id),
                new String[]{DownloadContract.Downloads.COLUMN_STATUS}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                // TODO: increase strictness of value returned for unknown
                // downloads; this is safe default for now.
                return DownloadStatus.PENDING;
            }
        } finally {
            cursor.close();
        }
    }

    public boolean hasTotalBytes() {
        return totalBytes != UNKNOWN_BYTES;
    }

    public boolean hasUnknownTotalBytes() {
        return !hasTotalBytes();
    }

    private void addHeader(String header, String value) {
        requestHeaders.add(Pair.create(header, value));
    }

    private void clearHeaders() {
        requestHeaders.clear();
    }

    public boolean isResumable() {
        return alwaysResume || (eTag != null && isNoIntegrity());
    }

    public static class Reader {
        private final ContentResolver resolver;
        private final Cursor cursor;

        public Reader(ContentResolver resolver, Cursor cursor) {
            this.resolver = resolver;
            this.cursor = cursor;
        }

        public FileDownloadInfo newDownloadInfo(SystemFacade systemFacade, DownloadsUriProvider downloadsUriProvider) {
            RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator();
            FileDownloadInfo info = new FileDownloadInfo(systemFacade, randomNumberGenerator, downloadsUriProvider);
            updateFromDatabase(info);
            readRequestHeaders(info);

            return info;
        }

        public void updateFromDatabase(FileDownloadInfo info) {
            info.id = getLong(DownloadContract.Downloads._ID);
            info.uri = getString(DownloadContract.Downloads.COLUMN_URI);
            info.scannable = getInt(DownloadContract.Downloads.COLUMN_MEDIA_SCANNED) == 1;
            info.noIntegrity = getInt(DownloadContract.Downloads.COLUMN_NO_INTEGRITY) == 1;
            info.hint = getString(DownloadContract.Downloads.COLUMN_FILE_NAME_HINT);
            info.fileName = getString(DownloadContract.Downloads.COLUMN_DATA);
            info.mimeType = getString(DownloadContract.Downloads.COLUMN_MIME_TYPE);
            info.destination = getInt(DownloadContract.Downloads.COLUMN_DESTINATION);
            info.status = getInt(DownloadContract.Downloads.COLUMN_STATUS);
            info.numFailed = getInt(DownloadContract.Downloads.COLUMN_FAILED_CONNECTIONS);
            int retryRedirect = getInt(Constants.RETRY_AFTER_X_REDIRECT_COUNT);
            info.retryAfter = retryRedirect & 0xfffffff;
            info.lastMod = getLong(DownloadContract.Downloads.COLUMN_LAST_MODIFICATION);
            info.notificationClassName = getString(DownloadContract.Downloads.COLUMN_NOTIFICATION_CLASS);
            info.extras = getString(DownloadContract.Downloads.COLUMN_NOTIFICATION_EXTRAS);
            info.cookies = getString(DownloadContract.Downloads.COLUMN_COOKIE_DATA);
            info.userAgent = getString(DownloadContract.Downloads.COLUMN_USER_AGENT);
            info.referer = getString(DownloadContract.Downloads.COLUMN_REFERER);
            info.totalBytes = getLong(DownloadContract.Downloads.COLUMN_TOTAL_BYTES);
            info.currentBytes = getLong(DownloadContract.Downloads.COLUMN_CURRENT_BYTES);
            info.eTag = getString(Constants.ETAG);
            info.uid = getInt(Constants.UID);
            info.mediaScanned = getInt(Constants.MEDIA_SCANNED);
            info.deleted = getInt(DownloadContract.Downloads.COLUMN_DELETED) == 1;
            info.mediaProviderUri = getString(DownloadContract.Downloads.COLUMN_MEDIAPROVIDER_URI);
            info.allowedNetworkTypes = getInt(DownloadContract.Downloads.COLUMN_ALLOWED_NETWORK_TYPES);
            info.allowRoaming = getInt(DownloadContract.Downloads.COLUMN_ALLOW_ROAMING) != 0;
            info.allowMetered = getInt(DownloadContract.Downloads.COLUMN_ALLOW_METERED) != 0;
            info.bypassRecommendedSizeLimit = getInt(DownloadContract.Downloads.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT);
            info.batchId = getLong(DownloadContract.Downloads.COLUMN_BATCH_ID);
            info.alwaysResume = getInt(DownloadContract.Downloads.COLUMN_ALWAYS_RESUME) != 0;

            synchronized (this) {
                info.control = getInt(DownloadContract.Downloads.COLUMN_CONTROL);
            }
        }

        private void readRequestHeaders(FileDownloadInfo info) {
            info.clearHeaders();
            Uri headerUri = Uri.withAppendedPath(info.getAllDownloadsUri(), DownloadContract.RequestHeaders.URI_SEGMENT);
            Cursor cursor = resolver.query(headerUri, null, null, null, null);
            try {
                int headerIndex =
                        cursor.getColumnIndexOrThrow(DownloadContract.RequestHeaders.COLUMN_HEADER);
                int valueIndex =
                        cursor.getColumnIndexOrThrow(DownloadContract.RequestHeaders.COLUMN_VALUE);
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

    static final class ControlStatus {

        private int control;
        private int status;

        public ControlStatus(int control, int status) {
            this.control = control;
            this.status = status;
        }

        public boolean isPaused() {
            return control == DownloadsControl.CONTROL_PAUSED;
        }

        public boolean isCanceled() {
            return status == DownloadStatus.CANCELED;
        }

        static final class Reader {

            private final ContentResolver contentResolver;
            private final DownloadsUriProvider downloadsUriProvider;

            public Reader(ContentResolver contentResolver, DownloadsUriProvider downloadsUriProvider) {
                this.contentResolver = contentResolver;
                this.downloadsUriProvider = downloadsUriProvider;
            }

            public ControlStatus newControlStatus(long id) {
                String[] projection = {DownloadContract.Downloads.COLUMN_CONTROL, DownloadContract.Downloads.COLUMN_STATUS};
                Uri uri = ContentUris.withAppendedId(downloadsUriProvider.getAllDownloadsUri(), id);
                Cursor downloadsCursor = contentResolver.query(uri, projection, null, null, null);
                try {
                    downloadsCursor.moveToFirst();
                    int control = downloadsCursor.getInt(0);
                    int status = downloadsCursor.getInt(1);
                    return new FileDownloadInfo.ControlStatus(control, status);
                } finally {
                    downloadsCursor.close();
                }
            }
        }
    }
}
