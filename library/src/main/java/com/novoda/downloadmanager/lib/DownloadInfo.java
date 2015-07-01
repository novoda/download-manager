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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Add to check if scannable i.e. we don't want internal files to be scanned
     */
    public boolean isScannable() {
        return scannable;
    }

    public void setScannable(boolean scannable) {
        this.scannable = scannable;
    }

    public boolean isNoIntegrity() {
        return noIntegrity;
    }

    public void setNoIntegrity(boolean noIntegrity) {
        this.noIntegrity = noIntegrity;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
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

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getControl() {
        return control;
    }

    public void setControl(int control) {
        this.control = control;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNumFailed() {
        return numFailed;
    }

    public void setNumFailed(int numFailed) {
        this.numFailed = numFailed;
    }

    public int getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(int retryAfter) {
        this.retryAfter = retryAfter;
    }

    public long getLastMod() {
        return lastMod;
    }

    public void setLastMod(long lastMod) {
        this.lastMod = lastMod;
    }

    public String getNotificationClassName() {
        return notificationClassName;
    }

    public void setNotificationClassName(String notificationClassName) {
        this.notificationClassName = notificationClassName;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public String getCookies() {
        return cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
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

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getMediaScanned() {
        return mediaScanned;
    }

    public void setMediaScanned(int mediaScanned) {
        this.mediaScanned = mediaScanned;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getMediaProviderUri() {
        return mediaProviderUri;
    }

    public void setMediaProviderUri(String mediaProviderUri) {
        this.mediaProviderUri = mediaProviderUri;
    }

    public int getAllowedNetworkTypes() {
        return allowedNetworkTypes;
    }

    public void setAllowedNetworkTypes(int allowedNetworkTypes) {
        this.allowedNetworkTypes = allowedNetworkTypes;
    }

    public boolean isAllowRoaming() {
        return allowRoaming;
    }

    public void setAllowRoaming(boolean allowRoaming) {
        this.allowRoaming = allowRoaming;
    }

    public boolean isAllowMetered() {
        return allowMetered;
    }

    public void setAllowMetered(boolean allowMetered) {
        this.allowMetered = allowMetered;
    }

    public int getBypassRecommendedSizeLimit() {
        return bypassRecommendedSizeLimit;
    }

    public void setBypassRecommendedSizeLimit(int bypassRecommendedSizeLimit) {
        this.bypassRecommendedSizeLimit = bypassRecommendedSizeLimit;
    }

    public long getBatchId() {
        return batchId;
    }

    public void setBatchId(long batchId) {
        this.batchId = batchId;
    }

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

    /**
     * For intents used to notify the user that a download exceeds a size threshold, if this extra
     * is true, WiFi is required for this download size; otherwise, it is only recommended.
     */
    public static final String EXTRA_IS_WIFI_REQUIRED = "isWifiRequired";

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
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, getId());
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_STATUS, finalStatus);
        intent.setData(getMyDownloadsUri());
        if (getExtras() != null) {
            intent.putExtra(EXTRA_EXTRA, getExtras());
        }
        context.sendBroadcast(intent);
    }

    private String getPackageName() {
        return context.getApplicationContext().getPackageName();
    }

    public void broadcastIntentDownloadFailedInsufficientSpace() {
        Intent intent = new Intent(DownloadManager.ACTION_DOWNLOAD_INSUFFICIENT_SPACE);
        intent.setPackage(getPackageName());
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, getId());
        intent.setData(getMyDownloadsUri());
        if (getExtras() != null) {
            intent.putExtra(EXTRA_EXTRA, getExtras());
        }
        context.sendBroadcast(intent);
    }

    /**
     * Returns the time when a download should be restarted.
     */
    public long restartTime(long now) {
        if (getNumFailed() == 0) {
            return now;
        }
        if (getRetryAfter() > 0) {
            return getLastMod() + getRetryAfter();
        }
        return getLastMod() + Constants.RETRY_FIRST_DELAY * (1000 + randomNumberGenerator.generate()) * (1 << (getNumFailed() - 1));
    }

    /**
     * Returns whether this download should be enqueued.
     */
    private boolean isDownloadManagerReadyToDownload() {
        if (getControl() == Downloads.Impl.CONTROL_PAUSED) {
            // the download is paused, so it's not going to start
            return false;
        }
        switch (getStatus()) {
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
        if (systemFacade.isActiveNetworkMetered() && !isAllowMetered()) {
            return NetworkState.TYPE_DISALLOWED_BY_REQUESTOR;
        }
        return checkIsNetworkTypeAllowed(info.getType());
    }

    private boolean isRoamingAllowed() {
        return isAllowRoaming();
    }

    /**
     * Check if this download can proceed over the given network type.
     *
     * @param networkType a constant from ConnectivityManager.TYPE_*.
     * @return one of the NETWORK_* constants
     */
    private NetworkState checkIsNetworkTypeAllowed(int networkType) {
        if (getTotalBytes() <= 0) {
            return NetworkState.OK; // we don't know the size yet
        }
        if (networkType == ConnectivityManager.TYPE_WIFI) {
            return NetworkState.OK; // anything goes over wifi
        }
        Long maxBytesOverMobile = systemFacade.getMaxBytesOverMobile();
        if (maxBytesOverMobile != null && getTotalBytes() > maxBytesOverMobile) {
            return NetworkState.UNUSABLE_DUE_TO_SIZE;
        }
        if (getBypassRecommendedSizeLimit() == 0) {
            Long recommendedMaxBytesOverMobile = systemFacade.getRecommendedMaxBytesOverMobile();
            if (recommendedMaxBytesOverMobile != null
                    && getTotalBytes() > recommendedMaxBytesOverMobile) {
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
        if (getTotalBytes() <= 0) {
            return NetworkState.OK; // we don't know the size yet
        }
        if (networkType == ConnectivityManager.TYPE_WIFI) {
            return NetworkState.OK; // anything goes over wifi
        }
        Long maxBytesOverMobile = systemFacade.getMaxBytesOverMobile();
        if (maxBytesOverMobile != null && getTotalBytes() > maxBytesOverMobile) {
            return NetworkState.UNUSABLE_DUE_TO_SIZE;
        }
        if (getBypassRecommendedSizeLimit() == 0) {
            Long recommendedMaxBytesOverMobile = systemFacade.getRecommendedMaxBytesOverMobile();
            if (recommendedMaxBytesOverMobile != null
                    && getTotalBytes() > recommendedMaxBytesOverMobile) {
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
        this.setStatus(status);
        downloadStatusContentValues.clear();
        downloadStatusContentValues.put(Downloads.Impl.COLUMN_STATUS, this.getStatus());
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
        return (getDestination() == Downloads.Impl.DESTINATION_CACHE_PARTITION
                || getDestination() == Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION
                || getDestination() == Downloads.Impl.DESTINATION_CACHE_PARTITION_NOROAMING
                || getDestination() == Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE);
    }

    public Uri getMyDownloadsUri() {
        return ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, getId());
    }

    public Uri getAllDownloadsUri() {
        return ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, getId());
    }

    /**
     * Return time when this download will be ready for its next action, in
     * milliseconds after given time.
     *
     * @return If {@code 0}, download is ready to proceed immediately. If
     * {@link Long#MAX_VALUE}, then download has no future actions.
     */
    public long nextActionMillis(long now) {
        if (Downloads.Impl.isStatusCompleted(getStatus())) {
            return Long.MAX_VALUE;
        }
        if (getStatus() != Downloads.Impl.STATUS_WAITING_TO_RETRY) {
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
        return (getMediaScanned() == 0)
                && (getDestination() == Downloads.Impl.DESTINATION_EXTERNAL ||
                getDestination() == Downloads.Impl.DESTINATION_FILE_URI ||
                getDestination() == Downloads.Impl.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD)
                && Downloads.Impl.isStatusSuccess(getStatus())
                && isScannable();
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
        return getTotalBytes() != UNKNOWN_BYTES;
    }

    void addHeader(String header, String value) {
        requestHeaders.add(Pair.create(header, value));
    }

    void clearHeaders() {
        requestHeaders.clear();
    }

}
