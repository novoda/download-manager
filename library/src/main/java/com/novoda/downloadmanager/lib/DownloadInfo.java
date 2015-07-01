package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Stores information about an individual download.
 */
class DownloadInfo {
    public static final String EXTRA_EXTRA = "com.novoda.download.lib.KEY_INTENT_EXTRA";
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
        BLOCKED;
    }

    /**
     * For intents used to notify the user that a download exceeds a size threshold, if this extra
     * is true, WiFi is required for this download size; otherwise, it is only recommended.
     */
    public static final String EXTRA_IS_WIFI_REQUIRED = "isWifiRequired";

    public long id;
    public String uri;
    /**
     * Add to check if scannable i.e. we don't want internal files to be scanned
     */
    public boolean scannable;
    public boolean noIntegrity;
    public String hint;
    public String fileName;
    public String mimeType;
    public int destination;
    public int control;
    public int status;
    public int numFailed;
    public int retryAfter;
    public long lastMod;
    public String notificationClassName;
    public String extras;
    public String cookies;
    public String userAgent;
    public String referer;
    public long totalBytes;
    public long currentBytes;
    public String eTag;
    public int uid;
    public int mediaScanned;
    public boolean deleted;
    public String mediaProviderUri;
    public int allowedNetworkTypes;
    public boolean allowRoaming;
    public boolean allowMetered;
    public int bypassRecommendedSizeLimit;
    public long batchId;

    /**
     * Result of last {DownloadThread} started by
     * {@link #isReadyToDownload(CollatedDownloadInfo)} && {@link #startDownloadIfNotActive(ExecutorService)}.
     */
    private Future<?> submittedThread;

    private final List<Pair<String, String>> requestHeaders = new ArrayList<>();
    private final Context context;
    private final SystemFacade systemFacade;
    private final StorageManager storageManager;
    private final DownloadNotifier downloadNotifier;
    private final DownloadClientReadyChecker downloadClientReadyChecker;
    private final RandomNumberGenerator randomNumberGenerator;
    private final ContentValues downloadStatusContentValues;

    DownloadInfo(
            Context context,
            SystemFacade systemFacade,
            StorageManager storageManager,
            DownloadNotifier notifier,
            RandomNumberGenerator randomNumberGenerator,
            DownloadClientReadyChecker downloadClientReadyChecker,
            ContentValues downloadStatusContentValues) {
        this.context = context;
        this.systemFacade = systemFacade;
        this.storageManager = storageManager;
        this.downloadNotifier = notifier;
        this.randomNumberGenerator = randomNumberGenerator;
        this.downloadClientReadyChecker = downloadClientReadyChecker;
        this.downloadStatusContentValues = downloadStatusContentValues;
    }

    public Collection<Pair<String, String>> getHeaders() {
        return Collections.unmodifiableList(requestHeaders);
    }

    public void broadcastIntentDownloadComplete(int finalStatus) {
        Intent intent = new Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intent.setPackage(getPackageName());
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, id);
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_STATUS, finalStatus);
        intent.setData(getMyDownloadsUri());
        if (extras != null) {
            intent.putExtra(EXTRA_EXTRA, extras);
        }
        context.sendBroadcast(intent);
    }

    private String getPackageName() {
        return context.getApplicationContext().getPackageName();
    }

    public void broadcastIntentDownloadFailedInsufficientSpace() {
        Intent intent = new Intent(DownloadManager.ACTION_DOWNLOAD_INSUFFICIENT_SPACE);
        intent.setPackage(getPackageName());
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, id);
        intent.setData(getMyDownloadsUri());
        if (extras != null) {
            intent.putExtra(EXTRA_EXTRA, extras);
        }
        context.sendBroadcast(intent);
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
     * Returns whether this download should be enqueued.
     */
    private boolean isDownloadManagerReadyToDownload() {
        if (control == Downloads.Impl.CONTROL_PAUSED) {
            // the download is paused, so it's not going to start
            return false;
        }
        switch (status) {
            case 0: // status hasn't been initialized yet, this is a new download
            case Downloads.Impl.STATUS_PENDING: // download is explicit marked as ready to start
            case Downloads.Impl.STATUS_RUNNING: // download interrupted (process killed etc) while
                // running, without a chance to update the database
                return true;

            case Downloads.Impl.STATUS_WAITING_FOR_NETWORK:
            case Downloads.Impl.STATUS_QUEUED_FOR_WIFI:
                return checkCanUseNetwork() == NetworkState.OK;

            case Downloads.Impl.STATUS_WAITING_TO_RETRY:
                // download was waiting for a delayed restart
                final long now = systemFacade.currentTimeMillis();
                return restartTime(now) <= now;
            case Downloads.Impl.STATUS_DEVICE_NOT_FOUND_ERROR:
                // is the media mounted?
                return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            case Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR:
                // avoids repetition of retrying download
                return false;
        }
        return false;
    }

    /**
     * Returns whether this download is allowed to use the network.
     */
    public NetworkState checkCanUseNetwork() {
        final NetworkInfo info = systemFacade.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return NetworkState.NO_CONNECTION;
        }
        if (NetworkInfo.DetailedState.BLOCKED.equals(info.getDetailedState())) {
            return NetworkState.BLOCKED;
        }
        if (systemFacade.isNetworkRoaming() && !isRoamingAllowed()) {
            return NetworkState.CANNOT_USE_ROAMING;
        }
        if (systemFacade.isActiveNetworkMetered() && !allowMetered) {
            return NetworkState.TYPE_DISALLOWED_BY_REQUESTOR;
        }
        return checkIsNetworkTypeAllowed(info.getType());
    }

    private boolean isRoamingAllowed() {
        return allowRoaming;
    }

    /**
     * Check if this download can proceed over the given network type.
     *
     * @param networkType a constant from ConnectivityManager.TYPE_*.
     * @return one of the NETWORK_* constants
     */
    private NetworkState checkIsNetworkTypeAllowed(int networkType) {
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

    /**
     * If download is ready to start, and isn't already pending or executing,
     * create a {DownloadThread} and enqueue it into given
     * {@link Executor}.
     *
     * @param collatedDownloadInfo
     * @return If actively downloading.
     */
    public boolean isReadyToDownload(CollatedDownloadInfo collatedDownloadInfo) {
        synchronized (this) {
            return isClientReadyToDownload(collatedDownloadInfo) && isDownloadManagerReadyToDownload();
        }
    }

    public boolean startDownloadIfNotActive(ExecutorService executor) {
        synchronized (this) {
            boolean isActive;
            if (submittedThread == null) {
                ContentResolver contentResolver = context.getContentResolver();
                BatchStatusRepository batchStatusRepository = new BatchStatusRepository(contentResolver);
                DownloadThread downloadThread = new DownloadThread(context, systemFacade, this, storageManager, downloadNotifier, batchStatusRepository);
                submittedThread = executor.submit(downloadThread);
                isActive = true;
            } else {
                isActive = !submittedThread.isDone();
            }
            return isActive;
        }
    }

    public boolean isActive() {
        return submittedThread != null && !submittedThread.isDone();
    }

    public void updateStatus(int status) {
        this.status = status;
        downloadStatusContentValues.clear();
        downloadStatusContentValues.put(Downloads.Impl.COLUMN_STATUS, this.status);
        context.getContentResolver().update(getAllDownloadsUri(), downloadStatusContentValues, null, null);
    }

    private boolean isClientReadyToDownload(CollatedDownloadInfo collatedDownloadInfo) {
        return downloadClientReadyChecker.isAllowedToDownload(collatedDownloadInfo);
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
        return (destination == Downloads.Impl.DESTINATION_CACHE_PARTITION
                || destination == Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION
                || destination == Downloads.Impl.DESTINATION_CACHE_PARTITION_NOROAMING
                || destination == Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE);
    }

    public Uri getMyDownloadsUri() {
        return ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, id);
    }

    public Uri getAllDownloadsUri() {
        return ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id);
    }

    /**
     * Return time when this download will be ready for its next action, in
     * milliseconds after given time.
     *
     * @return If {@code 0}, download is ready to proceed immediately. If
     * {@link Long#MAX_VALUE}, then download has no future actions.
     */
    public long nextActionMillis(long now) {
        if (Downloads.Impl.isStatusCompleted(status)) {
            return Long.MAX_VALUE;
        }
        if (status != Downloads.Impl.STATUS_WAITING_TO_RETRY) {
            return 0;
        }
        long when = restartTime(now);
        if (when <= now) {
            return 0;
        }
        return when - now;
    }

    /**
     * Returns whether a file should be scanned
     */
    public boolean shouldScanFile() {
        return (mediaScanned == 0)
                && (destination == Downloads.Impl.DESTINATION_EXTERNAL ||
                destination == Downloads.Impl.DESTINATION_FILE_URI ||
                destination == Downloads.Impl.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD)
                && Downloads.Impl.isStatusSuccess(status)
                && scannable;
    }

    void notifyPauseDueToSize(boolean isWifiRequired) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(getAllDownloadsUri());
        intent.setClassName(SizeLimitActivity.class.getPackage().getName(), SizeLimitActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_IS_WIFI_REQUIRED, isWifiRequired);
        context.startActivity(intent);
    }

    /**
     * Query and return status of requested download.
     */
    public static int queryDownloadStatus(ContentResolver resolver, long id) {
        final Cursor cursor = resolver.query(
                ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id),
                new String[]{Downloads.Impl.COLUMN_STATUS}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                // TODO: increase strictness of value returned for unknown
                // downloads; this is safe default for now.
                return Downloads.Impl.STATUS_PENDING;
            }
        } finally {
            cursor.close();
        }
    }

    public boolean hasTotalBytes() {
        return totalBytes != UNKNOWN_BYTES;
    }

    void addHeader(String header, String value) {
        requestHeaders.add(Pair.create(header, value));
    }

    void clearHeaders() {
        requestHeaders.clear();
    }

}
