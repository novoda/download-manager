package com.novoda.downloadmanager.lib;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;

import com.novoda.downloadmanager.notifications.NotificationVisibility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all the information necessary to request a new download. The URI is the
 * only required parameter.
 * <p/>
 * Note that the default download destination is a shared volume where the system might delete
 * your file if it needs to reclaim space for system use. If this is a problem, use a location
 * on external storage (see {@link #setDestinationUri(android.net.Uri)}.
 */
public class Request {
    /**
     * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
     * {@link android.net.ConnectivityManager#TYPE_MOBILE}.
     */
    public static final int NETWORK_MOBILE = 1 << 0;

    /**
     * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
     * {@link android.net.ConnectivityManager#TYPE_WIFI}.
     */
    public static final int NETWORK_WIFI = 1 << 1;

    /**
     * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
     * {@link android.net.ConnectivityManager#TYPE_BLUETOOTH}.
     */
    public static final int NETWORK_BLUETOOTH = 1 << 2;

    private final List<Pair<String, String>> requestHeaders = new ArrayList<>();

    private Uri uri;
    private Uri destinationUri;
    private CharSequence title = "";
    private CharSequence description = "";
    private String mimeType;
    private int allowedNetworkTypes = ~0; // default to all network types allowed
    private boolean roamingAllowed = true;
    private boolean meteredAllowed = true;
    private boolean isVisibleInDownloadsUi = true;
    private boolean scannable = false;
    private String notificationExtras;
    private String bigPictureUrl = "";
    private long batchId = -1L;
    private String extraData;
    private boolean alwaysResume;
    private boolean allowTarUpdates;
    private boolean noIntegrity;

    /**
     * if a file is designated as a MediaScanner scannable file, the following value is
     * stored in the database column {@link DownloadContract.Downloads#COLUMN_MEDIA_SCANNED}.
     */
    static final int SCANNABLE_VALUE_YES = 0;
    // value of 1 is stored in the above column by DownloadProvider after it is scanned by
    // MediaScanner
    /**
     * if a file is designated as a file that should not be scanned by MediaScanner,
     * the following value is stored in the database column
     * {@link DownloadContract.Downloads#COLUMN_MEDIA_SCANNED}.
     */
    static final int SCANNABLE_VALUE_NO = 2;

    /**
     * can take any of the following values: {@link NotificationVisibility#HIDDEN}
     * {@link NotificationVisibility#ACTIVE_OR_COMPLETE}, {@link NotificationVisibility#ONLY_WHEN_ACTIVE},
     * {@link NotificationVisibility#ONLY_WHEN_COMPLETE}
     */
    private int notificationVisibility = NotificationVisibility.ONLY_WHEN_ACTIVE;

    /**
     * @param uri the HTTP URI to download.
     */
    public Request(Uri uri) {
        if (uri == null) {
            throw new NullPointerException();
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
            throw new IllegalArgumentException("Can only download HTTP/HTTPS URIs: " + uri);
        }
        this.uri = uri;
    }

    Request(String uriString) {
        uri = Uri.parse(uriString);
    }

    /**
     * Set the local destination for the downloaded file. Must be a file URI to a path on
     * external storage, and the calling application must have the WRITE_EXTERNAL_STORAGE
     * permission.
     * <p/>
     * The downloaded file is not scanned by MediaScanner.
     * But it can be made scannable by calling {@link #allowScanningByMediaScanner()}.
     * <p/>
     * By default, downloads are saved to a generated filename in the shared download cache and
     * may be deleted by the system at any time to reclaim space.
     *
     * @return this object
     */
    public Request setDestinationUri(Uri uri) {
        destinationUri = uri;
        return this;
    }

    /**
     * Set the local destination for the downloaded file to a path within
     * the application's external files directory (as returned by
     * {@link android.content.Context#getExternalFilesDir(String)}.
     * <p/>
     * The downloaded file is not scanned by MediaScanner. But it can be
     * made scannable by calling {@link #allowScanningByMediaScanner()}.
     *
     * @param dirType the directory type to pass to
     *                {@link Context#getExternalFilesDir(String)}
     * @param subPath the path within the external directory, including the
     *                destination filename
     * @return this object
     * @throws IllegalStateException If the external storage directory
     *                               cannot be found or created.
     */
    public Request setDestinationInExternalFilesDir(String dirType, String subPath) {
        final File file = GlobalState.getContext().getExternalFilesDir(dirType);
        if (file == null) {
            throw new IllegalStateException("Failed to get external storage files directory");
        } else if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() + " already exists and is not a directory");
            }
        } else {
            if (!file.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " + file.getAbsolutePath());
            }
        }
        setDestinationFromBase(file, subPath);
        return this;
    }

    /**
     * Set the local destination for the downloaded file to a path within
     * the application's internal files directory (as returned by
     * {@link new File(context.getFilesDir(), dirType)}.
     * <p/>
     * The downloaded file is not scanned by MediaScanner. But it can be
     * made scannable by calling {@link #allowScanningByMediaScanner()}.
     *
     * @param dirType the directory type to pass to
     *                {@link Context#getExternalFilesDir(String)}
     * @param subPath the path within the external directory, including the
     *                destination filename
     * @return this object
     * @throws IllegalStateException If the external storage directory
     *                               cannot be found or created.
     */
    public Request setDestinationInInternalFilesDir(String dirType, String subPath) {
        final File file = new File(GlobalState.getContext().getFilesDir(), dirType);
        if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() + " already exists and is not a directory");
            }
        } else {
            if (!file.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " + file.getAbsolutePath());
            }
        }
        setDestinationFromBase(file, subPath);
        return this;
    }

    /**
     * Set the local destination for the downloaded file to a path within
     * the public external storage directory (as returned by
     * {@link android.os.Environment#getExternalStoragePublicDirectory(String)}).
     * <p/>
     * The downloaded file is not scanned by MediaScanner. But it can be
     * made scannable by calling {@link #allowScanningByMediaScanner()}.
     *
     * @param dirType the directory type to pass to {@link android.os.Environment#getExternalStoragePublicDirectory(String)}
     * @param subPath the path within the external directory, including the
     *                destination filename
     * @return this object
     * @throws IllegalStateException If the external storage directory
     *                               cannot be found or created.
     */
    public Request setDestinationInExternalPublicDir(String dirType, String subPath) {
        File file = Environment.getExternalStoragePublicDirectory(dirType);
        if (file == null) {
            throw new IllegalStateException("Failed to get external storage public directory");
        } else if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() + " already exists and is not a directory");
            }
        } else {
            if (!file.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " + file.getAbsolutePath());
            }
        }
        setDestinationFromBase(file, subPath);
        return this;
    }

    private void setDestinationFromBase(File base, String subPath) {
        if (subPath == null) {
            throw new NullPointerException("subPath cannot be null");
        }
        destinationUri = Uri.withAppendedPath(Uri.fromFile(base), subPath);
    }

    /**
     * If the file to be downloaded is to be scanned by MediaScanner, this method
     * should be called before {@link DownloadManager#enqueue(Request)} is called.
     */
    public void allowScanningByMediaScanner() {
        scannable = true;
    }

    /**
     * Always attempt to resume the download, regardless of whether the server returns
     * a Etag header or not. **CAUTION** if the file has changed then this flag will
     * result in undefined behaviour.
     */
    public Request alwaysAttemptResume() {
        alwaysResume = true;
        return this;
    }

    /**
     * Automatically pause the download when reaching the end of the file instead of writing the last bytes to disk.
     * This allows for a resume of the download against an updated tar file.
     * If used this also sets {@code alwaysAttemptResume()}
     */
    public Request allowTarUpdates() {
        allowTarUpdates = true;
        alwaysAttemptResume();
        return this;
    }

    /**
     * When a ETag header is present, the application should check the integrity of the
     * downloaded file, otherwise the current download won't be able to be resumed
     */
    public Request applicationChecksFileIntegrity() {
        noIntegrity = true;
        return this;
    }

    /**
     * Add an HTTP header to be included with the download request.  The header will be added to
     * the end of the list.
     *
     * @param header HTTP header name
     * @param value  header value
     * @return this object
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2">HTTP/1.1
     * Message Headers</a>
     */
    public Request addRequestHeader(String header, String value) {
        if (header == null) {
            throw new NullPointerException("header cannot be null");
        }
        if (header.contains(":")) {
            throw new IllegalArgumentException("header may not contain ':'");
        }
        if (value == null) {
            value = "";
        }
        requestHeaders.add(Pair.create(header, value));
        return this;
    }

    /**
     * Set the title of this download, to be displayed in notifications (if enabled).  If no
     * title is given, a default one will be assigned based on the download filename, once the
     * download starts.
     *
     * @return this object
     */
    public Request setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    /**
     * Set a description of this download, to be displayed in notifications (if enabled)
     *
     * @return this object
     */
    public Request setDescription(CharSequence description) {
        this.description = description;
        return this;
    }

    /**
     * Set a drawable url that will be used for the Big Picture Style
     *
     * @param bigPictureUrl the drawable resource id
     * @return this object
     */
    public Request setBigPictureUrl(String bigPictureUrl) {
        this.bigPictureUrl = bigPictureUrl;
        return this;
    }

    /**
     * Set the ID of the batch that this request belongs to
     *
     * @param batchId the batch id
     * @return this object
     */
    Request setBatchId(long batchId) {
        this.batchId = batchId;
        return this;
    }

    /**
     * Set the MIME content type of this download.  This will override the content type declared
     * in the server's response.
     *
     * @return this object
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7">HTTP/1.1
     * Media Types</a>
     */
    public Request setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    /**
     * Control whether a system notification is posted by the download manager while this
     * download is running. If enabled, the download manager posts notifications about downloads
     * through the system {@link android.app.NotificationManager}. By default, a notification is
     * shown.
     * <p/>
     * If set to false, this requires the permission
     * android.permission.DOWNLOAD_WITHOUT_NOTIFICATION.
     *
     * @param show whether the download manager should show a notification for this download.
     * @return this object
     * @deprecated use {@link #setNotificationVisibility(int)}
     */
    @Deprecated
    public Request setShowRunningNotification(boolean show) {
        return (show) ? setNotificationVisibility(NotificationVisibility.ONLY_WHEN_ACTIVE) : setNotificationVisibility(NotificationVisibility.HIDDEN);
    }

    /**
     * Control whether a system notification is posted by the download manager while this
     * download is running or when it is completed.
     * If enabled, the download manager posts notifications about downloads
     * through the system {@link android.app.NotificationManager}.
     * By default, a notification is shown only when the download is in progress.
     * <p/>
     * It can take the following values: {@link NotificationVisibility#HIDDEN},
     * {@link NotificationVisibility#ONLY_WHEN_ACTIVE},
     * {@link NotificationVisibility#ONLY_WHEN_COMPLETE}.
     * <p/>
     * If set to {@link NotificationVisibility#HIDDEN}, this requires the permission
     * android.permission.DOWNLOAD_WITHOUT_NOTIFICATION.
     *
     * @param visibility the visibility setting value
     * @return this object
     */
    public Request setNotificationVisibility(int visibility) {
        notificationVisibility = visibility;
        return this;
    }

    /**
     * Restrict the types of networks over which this download may proceed.
     * By default, all network types are allowed. Consider using
     * {@link #setAllowedOverMetered(boolean)} instead, since it's more
     * flexible.
     *
     * @param flags any combination of the NETWORK_* bit flags.
     * @return this object
     */
    public Request setAllowedNetworkTypes(int flags) {
        allowedNetworkTypes = flags;
        return this;
    }

    /**
     * Set whether this download may proceed over a roaming connection.  By default, roaming is
     * allowed.
     *
     * @param allowed whether to allow a roaming connection to be used
     * @return this object
     */
    public Request setAllowedOverRoaming(boolean allowed) {
        roamingAllowed = allowed;
        return this;
    }

    /**
     * Set whether this download may proceed over a metered network
     * connection. By default, metered networks are allowed.
     *
     * @see android.net.ConnectivityManager#isActiveNetworkMetered()
     */
    public Request setAllowedOverMetered(boolean allow) {
        meteredAllowed = allow;
        return this;
    }

    /**
     * Set whether this download should be displayed in the system's Downloads UI. True by
     * default.
     *
     * @param isVisible whether to display this download in the Downloads UI
     * @return this object
     */
    public Request setVisibleInDownloadsUi(boolean isVisible) {
        isVisibleInDownloadsUi = isVisible;
        return this;
    }

    /**
     * @param extra data that will be passed to you in the Intent on download completion.
     */
    public Request setNotificationExtra(String extra) {
        notificationExtras = extra;
        return this;
    }

    /**
     * @param extra data you want to save alongside your download so you can query it later.
     */
    public Request setExtraData(String extra) {
        extraData = extra;
        return this;
    }

    long getBatchId() {
        return batchId;
    }

    /**
     * @return ContentValues to be passed to DownloadProvider.insert()
     */
    ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        assert uri != null;
        values.put(DownloadContract.Downloads.COLUMN_URI, uri.toString());

        if (destinationUri != null) {
            values.put(DownloadContract.Downloads.COLUMN_DESTINATION, DownloadsDestination.DESTINATION_FILE_URI);
            values.put(DownloadContract.Downloads.COLUMN_FILE_NAME_HINT, destinationUri.toString());
        } else {
            values.put(
                    DownloadContract.Downloads.COLUMN_DESTINATION,
                    DownloadsDestination.DESTINATION_CACHE_PARTITION_PURGEABLE);
        }
        // is the file supposed to be media-scannable?
        values.put(DownloadContract.Downloads.COLUMN_MEDIA_SCANNED, (scannable) ? SCANNABLE_VALUE_YES : SCANNABLE_VALUE_NO);

        if (!requestHeaders.isEmpty()) {
            encodeHttpHeaders(values);
        }

        putIfNonNull(values, DownloadContract.Downloads.COLUMN_MIME_TYPE, mimeType);

        values.put(DownloadContract.Downloads.COLUMN_ALLOWED_NETWORK_TYPES, allowedNetworkTypes);
        values.put(DownloadContract.Downloads.COLUMN_ALLOW_ROAMING, roamingAllowed);
        values.put(DownloadContract.Downloads.COLUMN_ALLOW_METERED, meteredAllowed);
        values.put(DownloadContract.Downloads.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, isVisibleInDownloadsUi);
        values.put(DownloadContract.Downloads.COLUMN_NOTIFICATION_EXTRAS, notificationExtras);
        values.put(DownloadContract.Downloads.COLUMN_BATCH_ID, batchId);
        values.put(DownloadContract.Downloads.COLUMN_EXTRA_DATA, extraData);
        values.put(DownloadContract.Downloads.COLUMN_ALWAYS_RESUME, alwaysResume);
        values.put(DownloadContract.Downloads.COLUMN_ALLOW_TAR_UPDATES, allowTarUpdates);
        values.put(DownloadContract.Downloads.COLUMN_NO_INTEGRITY, noIntegrity);

        return values;
    }

    private void encodeHttpHeaders(ContentValues values) {
        int index = 0;
        for (Pair<String, String> header : requestHeaders) {
            String headerString = header.first + ": " + header.second;
            values.put(DownloadContract.RequestHeaders.INSERT_KEY_PREFIX + index, headerString);
            index++;
        }
    }

    private void putIfNonNull(ContentValues contentValues, String key, Object value) {
        if (value != null) {
            contentValues.put(key, value.toString());
        }
    }

    RequestBatch asBatch() {
        RequestBatch requestBatch = new RequestBatch.Builder()
                .withTitle(title.toString())
                .withDescription(description.toString())
                .withBigPictureUrl(bigPictureUrl)
                .withVisibility(notificationVisibility)
                .build();
        requestBatch.addRequest(this);
        return requestBatch;
    }

    String getDestinationPath() {
        return destinationUri.toString();
    }
}
