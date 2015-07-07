/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.novoda.downloadmanager.lib;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;

import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

/**
 * The download manager is a system service that handles long-running HTTP downloads. Clients may
 * request that a URI be downloaded to a particular destination file. The download manager will
 * conduct the download in the background, taking care of HTTP interactions and retrying downloads
 * after failures or across connectivity changes and system reboots.
 * <p/>
 * Instances of this class should be obtained through
 * {@link android.content.Context#getSystemService(String)} by passing
 * {@link android.content.Context#DOWNLOAD_SERVICE}.
 * <p/>
 * Apps that request downloads through this API should register a broadcast receiver for
 * {@link #ACTION_NOTIFICATION_CLICKED} to appropriately handle when the user clicks on a running
 * download in a notification or from the downloads UI.
 * <p/>
 * Note that the application must have the {@link android.Manifest.permission#INTERNET}
 * permission to use this class.
 */
public class DownloadManager {

    /**
     * An identifier for a particular download, unique across the system.  Clients use this ID to
     * make subsequent calls related to the download.
     */
    public static final String COLUMN_ID = Downloads.Impl._ID;

    /**
     * The client-supplied title for this download.  This will be displayed in system notifications.
     * Defaults to the empty string.
     */
    public static final String COLUMN_TITLE = Downloads.Impl.Batches.COLUMN_TITLE;

    /**
     * The client-supplied description of this download.  This will be displayed in system
     * notifications.  Defaults to the empty string.
     */
    public static final String COLUMN_DESCRIPTION = Downloads.Impl.Batches.COLUMN_DESCRIPTION;

    /**
     * The ID of the batch that contains this download.
     */
    public static final String COLUMN_BATCH_ID = Downloads.Impl.COLUMN_BATCH_ID;

    /**
     * The extra supplied information for this download.
     */
    public static final String COLUMN_NOTIFICATION_EXTRAS = Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS;

    /**
     * The status of the batch that contains this download.
     */
    public static final String COLUMN_BATCH_STATUS = Downloads.Impl.Batches.COLUMN_STATUS;

    /**
     * URI to be downloaded.
     */
    public static final String COLUMN_URI = Downloads.Impl.COLUMN_URI;

    /**
     * Internet Media Type of the downloaded file.  If no value is provided upon creation, this will
     * initially be null and will be filled in based on the server's response once the download has
     * started.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc1590.txt">RFC 1590, defining Media Types</a>
     */
    public static final String COLUMN_MEDIA_TYPE = "media_type";

    /**
     * Total size of the download in bytes.  This will initially be -1 and will be filled in once
     * the download starts.
     */
    public static final String COLUMN_TOTAL_SIZE_BYTES = "total_size";

    /**
     * Uri where downloaded file will be stored.  If a destination is supplied by client, that URI
     * will be used here.  Otherwise, the value will initially be null and will be filled in with a
     * generated URI once the download has started.
     */
    public static final String COLUMN_LOCAL_URI = "local_uri";

    /**
     * The pathname of the file where the download is stored.
     */
    public static final String COLUMN_LOCAL_FILENAME = "local_filename";

    /**
     * Current status of the download, as one of the STATUS_* constants.
     */
    public static final String COLUMN_STATUS = Downloads.Impl.COLUMN_STATUS;

    /**
     * Provides more detail on the status of the download.  Its meaning depends on the value of
     * {@link #COLUMN_STATUS}.
     * <p/>
     * When {@link #COLUMN_STATUS} is {@link #STATUS_FAILED}, this indicates the type of error that
     * occurred.  If an HTTP error occurred, this will hold the HTTP status code as defined in RFC
     * 2616.  Otherwise, it will hold one of the ERROR_* constants.
     * <p/>
     * When {@link #COLUMN_STATUS} is {@link #STATUS_PAUSED}, this indicates why the download is
     * paused.  It will hold one of the PAUSED_* constants.
     * <p/>
     * If {@link #COLUMN_STATUS} is neither {@link #STATUS_FAILED} nor {@link #STATUS_PAUSED}, this
     * column's value is undefined.
     *
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1.1">RFC 2616
     * status codes</a>
     */
    public static final String COLUMN_REASON = "reason";

    /**
     * Number of bytes download so far.
     */
    public static final String COLUMN_BYTES_DOWNLOADED_SO_FAR = "bytes_so_far";

    /**
     * Timestamp when the download was last modified, in {@link System#currentTimeMillis
     * System.currentTimeMillis()} (wall clock time in UTC).
     */
    public static final String COLUMN_LAST_MODIFIED_TIMESTAMP = "last_modified_timestamp";

    /**
     * The URI to the corresponding entry in MediaProvider for this downloaded entry. It is
     * used to delete the entries from MediaProvider database when it is deleted from the
     * downloaded list.
     */
    public static final String COLUMN_MEDIAPROVIDER_URI = Downloads.Impl.COLUMN_MEDIAPROVIDER_URI;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is waiting to start.
     */
    public static final int STATUS_PENDING = 1 << 0;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is currently running.
     */
    public static final int STATUS_RUNNING = 1 << 1;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is waiting to retry or resume.
     */
    public static final int STATUS_PAUSED = 1 << 2;

    /**
     * Value of {@link #COLUMN_STATUS} when the download has successfully completed.
     */
    public static final int STATUS_SUCCESSFUL = 1 << 3;

    /**
     * Value of {@link #COLUMN_STATUS} when the download has failed (and will not be retried).
     */
    public static final int STATUS_FAILED = 1 << 4;

    /**
     * Value of COLUMN_ERROR_CODE when the download has completed with an error that doesn't fit
     * under any other error code.
     */
    public static final int ERROR_UNKNOWN = 1000;

    /**
     * Value of {@link #COLUMN_REASON} when a storage issue arises which doesn't fit under any
     * other error code. Use the more specific {@link #ERROR_INSUFFICIENT_SPACE} and
     * {@link #ERROR_DEVICE_NOT_FOUND} when appropriate.
     */
    public static final int ERROR_FILE_ERROR = 1001;

    /**
     * Value of {@link #COLUMN_REASON} when an HTTP code was received that download manager
     * can't handle.
     */
    public static final int ERROR_UNHANDLED_HTTP_CODE = 1002;

    /**
     * Value of {@link #COLUMN_REASON} when an error receiving or processing data occurred at
     * the HTTP level.
     */
    public static final int ERROR_HTTP_DATA_ERROR = 1004;

    /**
     * Value of {@link #COLUMN_REASON} when there were too many redirects.
     */
    public static final int ERROR_TOO_MANY_REDIRECTS = 1005;

    /**
     * Value of {@link #COLUMN_REASON} when there was insufficient storage space. Typically,
     * this is because the SD card is full.
     */
    public static final int ERROR_INSUFFICIENT_SPACE = 1006;

    /**
     * Value of {@link #COLUMN_REASON} when no external storage device was found. Typically,
     * this is because the SD card is not mounted.
     */
    public static final int ERROR_DEVICE_NOT_FOUND = 1007;

    /**
     * Value of {@link #COLUMN_REASON} when some possibly transient error occurred but we can't
     * resume the download.
     */
    public static final int ERROR_CANNOT_RESUME = 1008;

    /**
     * Value of {@link #COLUMN_REASON} when the requested destination file already exists (the
     * download manager will not overwrite an existing file).
     */
    public static final int ERROR_FILE_ALREADY_EXISTS = 1009;

    /**
     * Value of {@link #COLUMN_REASON} when the download is paused because some network error
     * occurred and the download manager is waiting before retrying the request.
     */
    public static final int PAUSED_WAITING_TO_RETRY = 1;

    /**
     * Value of {@link #COLUMN_REASON} when the download is waiting for network connectivity to
     * proceed.
     */
    public static final int PAUSED_WAITING_FOR_NETWORK = 2;

    /**
     * Value of {@link #COLUMN_REASON} when the download exceeds a size limit for downloads over
     * the mobile network and the download manager is waiting for a Wi-Fi connection to proceed.
     */
    public static final int PAUSED_QUEUED_FOR_WIFI = 3;

    /**
     * Value of {@link #COLUMN_REASON} when the download is paused for some other reason.
     */
    public static final int PAUSED_UNKNOWN = 4;

    /**
     * Broadcast intent action sent by the download manager when a download completes. The
     * download's ID is specified in the intent's data.
     */
    public static final String ACTION_DOWNLOAD_COMPLETE = "com.novoda.downloadmanager.DOWNLOAD_COMPLETE";

    /**
     * Broadcast intent action sent by the download manager when a batch completes. The
     * batch's ID is specified in the intent's data.
     */
    public static final String ACTION_BATCH_COMPLETE = BatchCompletionBroadcaster.ACTION_BATCH_COMPLETE;

    /**
     * Broadcast intent action sent by the download manager when a download wasn't started due to insufficient space
     */
    public static final String ACTION_DOWNLOAD_INSUFFICIENT_SPACE = "com.novoda.downloadmanager.DOWNLOAD_INSUFFICIENT_SPACE";

    /**
     * Broadcast intent action sent by the download manager when the user clicks on a running
     * download, either from a system notification. The download's content: uri is specified
     * in the intent's data if the click is associated with a single download,
     * or {@link DownloadManager#CONTENT_URI} if the notification is associated with
     * multiple downloads.
     */
    public static final String ACTION_NOTIFICATION_CLICKED = "com.novoda.downloadmaanger.DOWNLOAD_NOTIFICATION_CLICKED";

    /**
     * Intent extra included with {@link #ACTION_DOWNLOAD_COMPLETE} intents, indicating the ID (as a
     * long) of the download that just completed.
     */
    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";

    /**
     * Intent extra included with {@link #ACTION_BATCH_COMPLETE} intents, indicating the ID (as a
     * long) of the batch that just completed.
     */
    public static final String EXTRA_BATCH_ID = BatchCompletionBroadcaster.EXTRA_BATCH_ID;

    /**
     * Intent extra included with {@link #ACTION_DOWNLOAD_COMPLETE} intents, indicating the status code of the download that just completed.
     */
    public static final String EXTRA_DOWNLOAD_STATUS = "extra_download_status";

    /**
     * When clicks on multiple notifications are received, the following
     * provides an array of download ids corresponding to the download notification that was
     * clicked. It can be retrieved by the receiver of this
     * Intent using {@link android.content.Intent#getLongArrayExtra(String)}.
     */
    public static final String EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS = "extra_click_download_ids";

    /**
     * columns to request from DownloadProvider.
     */
    public static final String[] UNDERLYING_COLUMNS = new String[]{
            Downloads.Impl._ID,
            Downloads.Impl._DATA + " AS " + COLUMN_LOCAL_FILENAME,
            Downloads.Impl.COLUMN_MEDIAPROVIDER_URI,
            Downloads.Impl.COLUMN_DESTINATION,
            Downloads.Impl.COLUMN_URI,
            Downloads.Impl.COLUMN_STATUS,
            Downloads.Impl.COLUMN_DELETED,
            Downloads.Impl.COLUMN_FILE_NAME_HINT,
            Downloads.Impl.COLUMN_MIME_TYPE + " AS " + COLUMN_MEDIA_TYPE,
            Downloads.Impl.COLUMN_TOTAL_BYTES + " AS " + COLUMN_TOTAL_SIZE_BYTES,
            Downloads.Impl.COLUMN_LAST_MODIFICATION + " AS " + COLUMN_LAST_MODIFIED_TIMESTAMP,
            Downloads.Impl.COLUMN_CURRENT_BYTES + " AS " + COLUMN_BYTES_DOWNLOADED_SO_FAR,
            Downloads.Impl.COLUMN_BATCH_ID,
            Downloads.Impl.Batches.COLUMN_TITLE,
            Downloads.Impl.Batches.COLUMN_DESCRIPTION,
            Downloads.Impl.Batches.COLUMN_BIG_PICTURE,
            Downloads.Impl.Batches.COLUMN_VISIBILITY,
            Downloads.Impl.Batches.COLUMN_STATUS,
        /* add the following 'computed' columns to the cursor.
         * they are not 'returned' by the database, but their inclusion
         * eliminates need to have lot of methods in CursorTranslator
         */
            "'placeholder' AS " + COLUMN_LOCAL_URI,
            "'placeholder' AS " + COLUMN_REASON
    };

    private final ContentResolver mResolver;
    private final Downloads downloads;

    private Uri mBaseUri;

    public DownloadManager(Context context,
                           ContentResolver contentResolver,
                           boolean verboseLogging,
                           Downloads downloads) {
        this.mResolver = contentResolver;
        this.downloads = downloads;
        this.mBaseUri = downloads.getContentUri();
        GlobalState.setContext(context);
        GlobalState.setVerboseLogging(verboseLogging);
    }

    /**
     * Makes this object access the download provider through /all_downloads URIs rather than
     * /my_downloads URIs, for clients that have permission to do so.
     */
    void setAccessAllDownloads(boolean accessAllDownloads) {
        if (accessAllDownloads) {
            mBaseUri = downloads.getAllDownloadsContentUri();
        } else {
            mBaseUri = downloads.getContentUri();
        }
    }

    /**
     * Enqueue a new download.  The download will start automatically once the download manager is
     * ready to execute it and connectivity is available.
     *
     * @param request the parameters specifying this download
     * @return an ID for the download, unique across the system.  This ID is used to make future
     * calls related to this download.
     */
    public long enqueue(Request request) {
        RequestBatch batch = request.asBatch();
        long batchId = insert(batch);
        request.setBatchId(batchId);
        return insert(request);
    }

    private long insert(Request request) {
        ContentValues values = request.toContentValues();
        Uri downloadUri = mResolver.insert(downloads.getContentUri(), values);
        return ContentUris.parseId(downloadUri);
    }

    public void pauseBatch(long id) {
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_PAUSED);
        mResolver.update(downloads.getAllDownloadsContentUri(), values, COLUMN_BATCH_ID + "=?", new String[] { String.valueOf(id) });
    }

    public void resumeBatch(long id) {
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_RUN);
        values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PENDING);
        mResolver.update(downloads.getAllDownloadsContentUri(), values, COLUMN_BATCH_ID + "=?", new String[] { String.valueOf(id) });
    }

    /**
     * Cancel downloads and remove them from the download manager.  Each download will be stopped if
     * it was running, and it will no longer be accessible through the download manager.
     * If there is a downloaded file, partial or complete, it is deleted.
     *
     * @param ids the IDs of the downloads to remove
     * @return the number of downloads actually removed
     */
    public int removeDownloads(long... ids) {
        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException("called with nothing to remove. input param 'ids' can't be null");
        }
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_DELETED, 1);
        // if only one id is passed in, then include it in the uri itself.
        // this will eliminate a full database scan in the download service.
        if (ids.length == 1) {
            return mResolver.update(ContentUris.withAppendedId(mBaseUri, ids[0]), values, null, null);
        }
        return mResolver.update(mBaseUri, values, getWhereClauseForIds(ids), longArrayToStringArray(ids));
    }

    /**
     * Cancel batch downloads and remove them from the download manager.  Each download will be stopped if
     * it was running, and it will no longer be accessible through the download manager.
     * If there are any downloaded files, partial or complete, they will be deleted.
     *
     * @param ids the IDs of the batches to remove
     * @return the number of batches actually removed
     */
    public int removeBatches(long... ids) {
        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException("called with nothing to remove. input param 'ids' can't be null");
        }
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.Batches.COLUMN_DELETED, 1);
        // if only one id is passed in, then include it in the uri itself.
        // this will eliminate a full database scan in the download service.
        if (ids.length == 1) {
            return mResolver.update(ContentUris.withAppendedId(downloads.getBatchContentUri(), ids[0]), values, null, null);
        }
        return mResolver.update(downloads.getBatchContentUri(), values, getWhereClauseForIds(ids), longArrayToStringArray(ids));
    }

    /**
     * Query the download manager about downloads that have been requested.
     *
     * @param query parameters specifying filters for this query
     * @return a Cursor over the result set of downloads, with columns consisting of all the
     * COLUMN_* constants.
     */
    public Cursor query(Query query) {
        Cursor underlyingCursor = query.runQuery(mResolver, UNDERLYING_COLUMNS, downloads.getDownloadsByBatchUri());
        if (underlyingCursor == null) {
            return null;
        }
        return new CursorTranslator(underlyingCursor, downloads.getDownloadsByBatchUri());
    }

    /**
     * Open a downloaded file for reading.  The download must have completed.
     *
     * @param id the ID of the download
     * @return a read-only {@link ParcelFileDescriptor}
     * @throws FileNotFoundException if the destination file does not already exist
     */
    public ParcelFileDescriptor openDownloadedFile(long id) throws FileNotFoundException {
        return mResolver.openFileDescriptor(getDownloadUri(id), "r");
    }

    /**
     * Returns {@link Uri} for the given downloaded file id, if the file is
     * downloaded successfully. otherwise, null is returned.
     * <p/>
     * If the specified downloaded file is in external storage (for example, /sdcard dir),
     * then it is assumed to be safe for anyone to read and the returned {@link Uri} corresponds
     * to the filepath on sdcard.
     *
     * @param id the id of the downloaded file.
     * @return the {@link Uri} for the given downloaded file id, if download was successful. null
     * otherwise.
     */
    public Uri getUriForDownloadedFile(long id) {
        // to check if the file is in cache, get its destination from the database
        Query query = new Query().setFilterById(id);
        Cursor cursor = null;
        try {
            cursor = query(query);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                if (DownloadManager.STATUS_SUCCESSFUL == status) {
                    int indx = cursor.getColumnIndexOrThrow(
                            Downloads.Impl.COLUMN_DESTINATION);
                    int destination = cursor.getInt(indx);
                    // TODO: if we ever add API to DownloadManager to let the caller specify
                    // non-external storage for a downloaded file, then the following code
                    // should also check for that destination.
                    if (destination == Downloads.Impl.DESTINATION_CACHE_PARTITION ||
                            destination == Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION ||
                            destination == Downloads.Impl.DESTINATION_CACHE_PARTITION_NOROAMING ||
                            destination == Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE) {
                        // return private uri
                        return ContentUris.withAppendedId(downloads.getContentUri(), id);
                    } else {
                        // return public uri
                        String path = cursor.getString(
                                cursor.getColumnIndexOrThrow(COLUMN_LOCAL_FILENAME));
                        return Uri.fromFile(new File(path));
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // downloaded file not found or its status is not 'successfully completed'
        return null;
    }

    /**
     * Returns {@link Uri} for the given downloaded file id, if the file is
     * downloaded successfully. otherwise, null is returned.
     * <p/>
     * If the specified downloaded file is in external storage (for example, /sdcard dir),
     * then it is assumed to be safe for anyone to read and the returned {@link Uri} corresponds
     * to the filepath on sdcard.
     *
     * @param id the id of the downloaded file.
     * @return the {@link Uri} for the given downloaded file id, if download was successful. null
     * otherwise.
     */
    public String getMimeTypeForDownloadedFile(long id) {
        Query query = new Query().setFilterById(id);
        Cursor cursor = null;
        try {
            cursor = query(query);
            if (cursor == null) {
                return null;
            }
            while (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDIA_TYPE));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // downloaded file not found or its status is not 'successfully completed'
        return null;
    }

    /**
     * Restart the given downloads, which must have already completed (successfully or not).  This
     * method will only work when called from within the download manager's process.
     *
     * @param ids the IDs of the downloads
     */
    public void restartDownload(long... ids) {
        Cursor cursor = query(new Query().setFilterById(ids));
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                if (status != STATUS_SUCCESSFUL && status != STATUS_FAILED) {
                    throw new IllegalArgumentException(
                            "Cannot restart incomplete download: "
                                    + cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                }
            }
        } finally {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, 0);
        values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, -1);
        values.putNull(Downloads.Impl._DATA);
        values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PENDING);
        values.put(Downloads.Impl.COLUMN_FAILED_CONNECTIONS, 0);
        mResolver.update(mBaseUri, values, getWhereClauseForIds(ids), longArrayToStringArray(ids));
    }

    /**
     * Returns maximum size, in bytes, of downloads that may go over a mobile connection; or null if
     * there's no limit
     *
     * @param context the {@link Context} to use for accessing the {@link ContentResolver}
     * @return maximum size, in bytes, of downloads that may go over a mobile connection; or null if
     * there's no limit
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Long getMaxBytesOverMobile(Context context) {
        try {
            //Settings.Global.DOWNLOAD_MAX_BYTES_OVER_MOBILE
            return Settings.Global.getLong(context.getContentResolver(), "download_manager_max_bytes_over_mobile");
        } catch (SettingNotFoundException exc) {
            return null;
        }
    }

    /**
     * Returns recommended maximum size, in bytes, of downloads that may go over a mobile
     * connection; or null if there's no recommended limit.  The user will have the option to bypass
     * this limit.
     *
     * @param context the {@link Context} to use for accessing the {@link ContentResolver}
     * @return recommended maximum size, in bytes, of downloads that may go over a mobile
     * connection; or null if there's no recommended limit.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Long getRecommendedMaxBytesOverMobile(Context context) {
        try {
            //Settings.Global.DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE
            return Settings.Global.getLong(context.getContentResolver(), "download_manager_recommended_max_bytes_over_mobile");
        } catch (SettingNotFoundException exc) {
            return null;
        }
    }

    /**
     * Adds a file to the downloads database system, so it could appear in Downloads App
     * (and thus become eligible for management by the Downloads App).
     * <p/>
     * It is helpful to make the file scannable by MediaScanner by setting the param
     * isMediaScannerScannable to true. It makes the file visible in media managing
     * applications such as Gallery App, which could be a useful purpose of using this API.
     *
     * @param title                   the title that would appear for this file in Downloads App.
     * @param description             the description that would appear for this file in Downloads App.
     * @param isMediaScannerScannable true if the file is to be scanned by MediaScanner. Files
     *                                scanned by MediaScanner appear in the applications used to view media (for example,
     *                                Gallery app).
     * @param mimeType                mimetype of the file.
     * @param path                    absolute pathname to the file. The file should be world-readable, so that it can
     *                                be managed by the Downloads App and any other app that is used to read it (for example,
     *                                Gallery app to display the file, if the file contents represent a video/image).
     * @param length                  length of the downloaded file
     * @param showNotification        true if a notification is to be sent, false otherwise
     * @return an ID for the download entry added to the downloads app, unique across the system
     * This ID is used to make future calls related to this download.
     */
    // TODO: Add batch to request
    public long addCompletedDownload(String title, String description,
                                     boolean isMediaScannerScannable, String mimeType, String path, long length,
                                     boolean showNotification) {
        // make sure the input args are non-null/non-zero
        validateArgumentIsNonEmpty("title", title);
        validateArgumentIsNonEmpty("description", description);
        validateArgumentIsNonEmpty("path", path);
        validateArgumentIsNonEmpty("mimeType", mimeType);
        if (length < 0) {
            throw new IllegalArgumentException(" invalid value for param: totalBytes");
        }

        // if there is already an entry with the given path name in downloads.db, return its id
        Request request = new Request(NON_DOWNLOADMANAGER_DOWNLOAD)
                .setTitle(title)
                .setDescription(description)
                .setMimeType(mimeType)
                .setNotificationVisibility((showNotification) ? NotificationVisibility.ONLY_WHEN_COMPLETE : NotificationVisibility.HIDDEN);

        ContentValues values = request.toContentValues();
        values.put(Downloads.Impl.COLUMN_DESTINATION, Downloads.Impl.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD);
        values.put(Downloads.Impl._DATA, path);
        values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_SUCCESS);
        values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, length);
        values.put(Downloads.Impl.COLUMN_MEDIA_SCANNED, (isMediaScannerScannable) ? Request.SCANNABLE_VALUE_YES : Request.SCANNABLE_VALUE_NO);
        Uri downloadUri = mResolver.insert(downloads.getContentUri(), values);
        if (downloadUri == null) {
            return -1;
        }
        return ContentUris.parseId(downloadUri);
    }

    private static final String NON_DOWNLOADMANAGER_DOWNLOAD =
            "non-dwnldmngr-download-dont-retry2download";

    private static void validateArgumentIsNonEmpty(String paramName, String val) {
        if (TextUtils.isEmpty(val)) {
            throw new IllegalArgumentException(paramName + " can't be null");
        }
    }

    /**
     * Get the DownloadProvider URI for the download with the given ID.
     */
    private Uri getDownloadUri(long id) {
        return ContentUris.withAppendedId(mBaseUri, id);
    }

    /**
     * Get a parameterized SQL WHERE clause to select a bunch of IDs.
     */
    static String getWhereClauseForIds(long[] ids) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                whereClause.append("OR ");
            }
            whereClause.append(Downloads.Impl._ID);
            whereClause.append(" = ? ");
        }
        whereClause.append(")");
        return whereClause.toString();
    }

    /**
     * Get the selection args for a clause returned by {@link #getWhereClauseForIds(long[])}.
     */
    private static String[] longArrayToStringArray(long[] ids) {
        String[] whereArgs = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            whereArgs[i] = Long.toString(ids[i]);
        }
        return whereArgs;
    }

    /**
     * Enqueue a new download batch.
     *
     * @param batch the parameters specifying this batch
     * @return an ID for the batch, unique across the system.  This ID is used to make future
     * calls related to this batch.
     */
    public long enqueue(RequestBatch batch) {
        long batchId = insert(batch);
        for (Request request : batch.getRequests()) {
            request.setBatchId(batchId);
            insert(request);
        }
        return batchId;
    }

    private long insert(RequestBatch batch) {
        ContentValues values = batch.toContentValues();
        values.put(Downloads.Impl.Batches.COLUMN_STATUS, Downloads.Impl.STATUS_PENDING);
        Uri batchUri = mResolver.insert(downloads.getBatchContentUri(), values);
        return ContentUris.parseId(batchUri);
    }

    /**
     * This class wraps a cursor returned by DownloadProvider -- the "underlying cursor" -- and
     * presents a different set of columns, those defined in the DownloadManager.COLUMN_* constants.
     * Some columns correspond directly to underlying values while others are computed from
     * underlying data.
     */
    private static class CursorTranslator extends CursorWrapper {
        private Uri mBaseUri;

        public CursorTranslator(Cursor cursor, Uri baseUri) {
            super(cursor);
            mBaseUri = baseUri;
        }

        @Override
        public int getInt(int columnIndex) {
            return (int) getLong(columnIndex);
        }

        @Override
        public long getLong(int columnIndex) {
            String columnName = getColumnName(columnIndex);
            switch (columnName) {
                case COLUMN_REASON:
                    return getReason(super.getInt(getColumnIndex(Downloads.Impl.COLUMN_STATUS)));
                case COLUMN_STATUS:
                    return translateStatus(super.getInt(getColumnIndex(Downloads.Impl.COLUMN_STATUS)));
                case COLUMN_BATCH_STATUS:
                    return translateStatus(super.getInt(getColumnIndex(Downloads.Impl.Batches.COLUMN_STATUS)));
                default:
                    return super.getLong(columnIndex);
            }
        }

        @Override
        public String getString(int columnIndex) {
            return getColumnName(columnIndex).equals(COLUMN_LOCAL_URI) ? getLocalUri() : super.getString(columnIndex);
        }

        private String getLocalUri() {
            long destinationType = getLong(getColumnIndex(Downloads.Impl.COLUMN_DESTINATION));
            if (destinationType == Downloads.Impl.DESTINATION_FILE_URI
                    || destinationType == Downloads.Impl.DESTINATION_EXTERNAL
                    || destinationType == Downloads.Impl.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD) {
                String localPath = getString(getColumnIndex(COLUMN_LOCAL_FILENAME));
                if (localPath == null) {
                    return null;
                }
                return Uri.fromFile(new File(localPath)).toString();
            }

            // return content URI for cache download
            long downloadId = getLong(getColumnIndex(Downloads.Impl._ID));
            return ContentUris.withAppendedId(mBaseUri, downloadId).toString();
        }

        private long getReason(int status) {
            switch (translateStatus(status)) {
                case STATUS_FAILED:
                    return getErrorCode(status);

                case STATUS_PAUSED:
                    return getPausedReason(status);

                default:
                    return 0; // arbitrary value when status is not an error
            }
        }

        private long getPausedReason(int status) {
            switch (status) {
                case Downloads.Impl.STATUS_WAITING_TO_RETRY:
                    return PAUSED_WAITING_TO_RETRY;

                case Downloads.Impl.STATUS_WAITING_FOR_NETWORK:
                    return PAUSED_WAITING_FOR_NETWORK;

                case Downloads.Impl.STATUS_QUEUED_FOR_WIFI:
                    return PAUSED_QUEUED_FOR_WIFI;

                default:
                    return PAUSED_UNKNOWN;
            }
        }

        private long getErrorCode(int status) {
            if ((400 <= status && status < Downloads.Impl.MIN_ARTIFICIAL_ERROR_STATUS)
                    || (500 <= status && status < 600)) {
                // HTTP status code
                return status;
            }

            switch (status) {
                case Downloads.Impl.STATUS_FILE_ERROR:
                    return ERROR_FILE_ERROR;

                case Downloads.Impl.STATUS_UNHANDLED_HTTP_CODE:
                case Downloads.Impl.STATUS_UNHANDLED_REDIRECT:
                    return ERROR_UNHANDLED_HTTP_CODE;

                case Downloads.Impl.STATUS_HTTP_DATA_ERROR:
                    return ERROR_HTTP_DATA_ERROR;

                case Downloads.Impl.STATUS_TOO_MANY_REDIRECTS:
                    return ERROR_TOO_MANY_REDIRECTS;

                case Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR:
                    return ERROR_INSUFFICIENT_SPACE;

                case Downloads.Impl.STATUS_DEVICE_NOT_FOUND_ERROR:
                    return ERROR_DEVICE_NOT_FOUND;

                case Downloads.Impl.STATUS_CANNOT_RESUME:
                    return ERROR_CANNOT_RESUME;

                case Downloads.Impl.STATUS_FILE_ALREADY_EXISTS_ERROR:
                    return ERROR_FILE_ALREADY_EXISTS;

                default:
                    return ERROR_UNKNOWN;
            }
        }

        private int translateStatus(int status) {
            switch (status) {
                case Downloads.Impl.STATUS_PENDING:
                    return STATUS_PENDING;

                case Downloads.Impl.STATUS_RUNNING:
                    return STATUS_RUNNING;

                case Downloads.Impl.STATUS_PAUSED_BY_APP:
                case Downloads.Impl.STATUS_WAITING_TO_RETRY:
                case Downloads.Impl.STATUS_WAITING_FOR_NETWORK:
                case Downloads.Impl.STATUS_QUEUED_FOR_WIFI:
                    return STATUS_PAUSED;

                case Downloads.Impl.STATUS_SUCCESS:
                    return STATUS_SUCCESSFUL;

                default:
                    return STATUS_FAILED;
            }
        }
    }
}
