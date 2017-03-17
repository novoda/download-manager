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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;

import com.evernote.android.job.JobManager;
import com.novoda.downloadmanager.lib.jobscheduler.DownloadJob;
import com.novoda.downloadmanager.lib.jobscheduler.DownloadManagerJobCreator;
import com.novoda.downloadmanager.lib.logger.LLog;
import com.novoda.downloadmanager.notifications.NotificationVisibility;

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
    //CHECKSTYLE IGNORE MagicNumber

    /**
     * Extra information available when you register for notications of download status changes
     * see {@link Request#setNotificationExtra(String extra)`
     */
    public static final String EXTRA_EXTRA = "com.novoda.download.lib.KEY_INTENT_EXTRA";

    /**
     * An identifier for a particular download, unique across the system.  Clients use this ID to
     * make subsequent calls related to the download.
     */
    public static final String COLUMN_ID = DownloadContract.Downloads._ID;

    /**
     * The client-supplied title for this download.  This will be displayed in system notifications.
     * Defaults to the empty string.
     */
    public static final String COLUMN_TITLE = DownloadContract.Batches.COLUMN_TITLE;

    /**
     * The client-supplied description of this download.  This will be displayed in system
     * notifications.  Defaults to the empty string.
     */
    public static final String COLUMN_DESCRIPTION = DownloadContract.Batches.COLUMN_DESCRIPTION;

    /**
     * The ID of the batch that contains this download.
     */
    public static final String COLUMN_BATCH_ID = DownloadContract.Downloads.COLUMN_BATCH_ID;

    /**
     * The total size in bytes of the batch.
     */
    public static final String COLUMN_BATCH_TOTAL_SIZE_BYTES = DownloadContract.BatchesWithSizes.COLUMN_TOTAL_BYTES;

    /**
     * The current size in bytes of the batch.
     */
    public static final String COLUMN_BATCH_CURRENT_SIZE_BYTES = DownloadContract.BatchesWithSizes.COLUMN_CURRENT_BYTES;

    /**
     * The extra supplied information available to completion notifications for this download.
     */
    public static final String COLUMN_NOTIFICATION_EXTRAS = DownloadContract.Downloads.COLUMN_NOTIFICATION_EXTRAS;

    /**
     * The extra supplied information available with any query for this download.
     */
    public static final String COLUMN_EXTRA_DATA = DownloadContract.Downloads.COLUMN_EXTRA_DATA;

    /**
     * The status of the batch that contains this download.
     */
    public static final String COLUMN_BATCH_STATUS = DownloadContract.Batches.COLUMN_STATUS;

    /**
     * URI to be downloaded.
     */
    public static final String COLUMN_URI = DownloadContract.Downloads.COLUMN_URI;

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
    public static final String COLUMN_STATUS = DownloadContract.Downloads.COLUMN_STATUS;

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
    public static final String COLUMN_LAST_MODIFIED_TIMESTAMP = DownloadContract.Downloads.COLUMN_LAST_MODIFICATION;

    /**
     * The URI to the corresponding entry in MediaProvider for this downloaded entry. It is
     * used to delete the entries from MediaProvider database when it is deleted from the
     * downloaded list.
     */
    public static final String COLUMN_MEDIAPROVIDER_URI = DownloadContract.Downloads.COLUMN_MEDIAPROVIDER_URI;

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
     * Value of {@link #COLUMN_STATUS} when the download is marked for deletion.
     */
    public static final int STATUS_DELETING = 1 << 5;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is marked for pausing.
     */
    public static final int STATUS_PAUSING = 1 << 6;

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
     * Value of {@link #COLUMN_REASON} when the download is paused due client download check permissions.
     */
    public static final int PAUSED_QUEUED_DUE_CLIENT_RESTRICTIONS = 5;

    /**
     * Broadcast intent action sent by the download manager when a download completes. The
     * download's ID is specified in the intent's data.
     */
    public static final String ACTION_DOWNLOAD_COMPLETE = "com.novoda.downloadmanager.DOWNLOAD_COMPLETE";

    /**
     * Broadcast intent action sent by the download manager when a batch completes. The
     * batch's ID is specified in the intent's data.
     */
    public static final String ACTION_BATCH_COMPLETE = BatchInformationBroadcaster.ACTION_BATCH_COMPLETE;

    /**
     * Broadcast intent action sent by the download manager when a batch failed. The
     * batch's ID is specified in the intent's data.
     */
    public static final String ACTION_BATCH_FAILED = BatchInformationBroadcaster.ACTION_BATCH_FAILED;

    /**
     * Broadcast intent action sent by the download manager when a batch has started. The
     * batch's ID is specified in the intent's data.
     */
    public static final String ACTION_BATCH_STARTED = BatchInformationBroadcaster.ACTION_BATCH_STARTED_FOR_FIRST_TIME;

    /**
     * Broadcast intent action sent by the download manager when a download wasn't started due to insufficient space
     */
    public static final String ACTION_DOWNLOAD_INSUFFICIENT_SPACE = "com.novoda.downloadmanager.DOWNLOAD_INSUFFICIENT_SPACE";

    /**
     * Broadcast intent action sent by the download manager when the user clicks on a running
     * download, either from a system notification. The download's content: uri is specified
     * in the intent's data if the click is associated with a single download,
     * or {@link DownloadsUriProvider#getContentUri()} if the notification is associated with
     * multiple downloads.
     */
    public static final String ACTION_NOTIFICATION_CLICKED = "com.novoda.downloadmanager.DOWNLOAD_NOTIFICATION_CLICKED";

    /**
     * Intent extra included with {@link #ACTION_DOWNLOAD_COMPLETE} intents, indicating the ID (as a
     * long) of the download that just completed.
     */
    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";

    /**
     * Intent extra included with {@link #ACTION_BATCH_COMPLETE} intents, indicating the ID (as a
     * long) of the batch that just completed.
     */
    public static final String EXTRA_BATCH_ID = BatchInformationBroadcaster.EXTRA_BATCH_ID;

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
     * When clicks on multiple notifications are received, the following
     * provides an int array of download statuses corresponding to the download notification that was
     * clicked (in the same order as @{link DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS}.
     * It can be retrieved by the receiver of this Intent using {@link android.content.Intent#getLongArrayExtra(String)}.
     */
    public static final String EXTRA_NOTIFICATION_CLICK_DOWNLOAD_STATUSES = "download_statuses";

    /**
     * columns to request from DownloadProvider.
     */
    public static final String[] UNDERLYING_COLUMNS = new String[]{
            DownloadContract.Downloads._ID,
            DownloadContract.Downloads.COLUMN_DATA + " AS " + COLUMN_LOCAL_FILENAME,
            DownloadContract.Downloads.COLUMN_MEDIAPROVIDER_URI,
            DownloadContract.Downloads.COLUMN_DESTINATION,
            DownloadContract.Downloads.COLUMN_URI,
            DownloadContract.Downloads.COLUMN_STATUS,
            DownloadContract.Downloads.COLUMN_DELETED,
            DownloadContract.Downloads.COLUMN_FILE_NAME_HINT,
            DownloadContract.Downloads.COLUMN_MIME_TYPE + " AS " + COLUMN_MEDIA_TYPE,
            DownloadContract.Downloads.COLUMN_TOTAL_BYTES + " AS " + COLUMN_TOTAL_SIZE_BYTES,
            DownloadContract.Downloads.COLUMN_LAST_MODIFICATION,
            DownloadContract.Downloads.COLUMN_CURRENT_BYTES + " AS " + COLUMN_BYTES_DOWNLOADED_SO_FAR,
            DownloadContract.Downloads.COLUMN_BATCH_ID,
            DownloadContract.Downloads.COLUMN_EXTRA_DATA,
            DownloadContract.Downloads.COLUMN_NOTIFICATION_EXTRAS,
            DownloadContract.Batches.COLUMN_TITLE,
            DownloadContract.Batches.COLUMN_DESCRIPTION,
            DownloadContract.Batches.COLUMN_BIG_PICTURE,
            DownloadContract.Batches.COLUMN_VISIBILITY,
            DownloadContract.Batches.COLUMN_STATUS,
            DownloadContract.Batches.COLUMN_EXTRA_DATA,
            DownloadContract.Batches.COLUMN_LAST_MODIFICATION,
        /* add the following 'computed' columns to the cursor.
         * they are not 'returned' by the database, but their inclusion
         * eliminates need to have lot of methods in CursorTranslator
         */
            "'placeholder' AS " + COLUMN_LOCAL_URI,
            "'placeholder' AS " + COLUMN_REASON
    };

    //CHECKSTYLE END IGNORE MagicNumber

    private final ContentResolver contentResolver;
    private final DownloadsUriProvider downloadsUriProvider;
    private final SystemFacade systemFacade;
    private final BatchPauseResumeController batchPauseResumeController;

    private Uri baseUri;

    public DownloadManager(Context context, ContentResolver contentResolver, boolean verboseLogging) {
        this(
                context,
                contentResolver,
                DownloadsUriProvider.getInstance(),
                new RealSystemFacade(context, new Clock()),
                new BatchPauseResumeController(
                        contentResolver,
                        DownloadsUriProvider.getInstance(),
                        BatchRepository.from(
                                contentResolver,
                                new DownloadDeleter(contentResolver),
                                DownloadsUriProvider.getInstance(),
                                new RealSystemFacade(GlobalState.getContext(), new Clock())
                        ),
                        new DownloadsRepository(
                                new RealSystemFacade(GlobalState.getContext(), new Clock()), contentResolver,
                                DownloadsRepository.DownloadInfoCreator.NON_FUNCTIONAL,
                                DownloadsUriProvider.getInstance()
                        )
                ),
                verboseLogging
        );
    }

    DownloadManager(Context context, ContentResolver contentResolver, DownloadsUriProvider downloadsUriProvider) {
        this(
                context,
                contentResolver,
                downloadsUriProvider,
                new RealSystemFacade(context, new Clock()),
                new BatchPauseResumeController(
                        contentResolver,
                        DownloadsUriProvider.getInstance(),
                        BatchRepository.from(
                                contentResolver,
                                new DownloadDeleter(contentResolver),
                                DownloadsUriProvider.getInstance(),
                                new RealSystemFacade(GlobalState.getContext(), new Clock())
                        ),
                        new DownloadsRepository(
                                new RealSystemFacade(GlobalState.getContext(), new Clock()), contentResolver,
                                DownloadsRepository.DownloadInfoCreator.NON_FUNCTIONAL,
                                DownloadsUriProvider.getInstance()
                        )
                ),
                false
        );
    }

    DownloadManager(Context context,
                    ContentResolver contentResolver,
                    DownloadsUriProvider downloadsUriProvider,
                    SystemFacade systemFacade,
                    BatchPauseResumeController batchPauseResumeController,
                    boolean verboseLogging) {
        this.contentResolver = contentResolver;
        this.downloadsUriProvider = downloadsUriProvider;
        this.baseUri = downloadsUriProvider.getContentUri();
        this.systemFacade = systemFacade;
        this.batchPauseResumeController = batchPauseResumeController;
        GlobalState.setContext(context);
        GlobalState.setVerboseLogging(verboseLogging);
        JobManager.create(context).addJobCreator(new DownloadManagerJobCreator());
    }

    /**
     * Create an intent which will cancel the batch with the supplied ID. Must be sent as a broadcast.
     *
     * @param batchId the ID of the batch to delete
     * @return an {@link Intent} that can be broadcast
     */
    public static Intent createCancelBatchIntent(long batchId, Context context) {
        Intent cancelIntent = new Intent(Constants.ACTION_CANCEL, null, context, DownloadReceiver.class);
        cancelIntent.putExtra(DownloadReceiver.EXTRA_BATCH_ID, batchId);
        return cancelIntent;
    }

    /**
     * Makes this object access the download provider through /all_downloads URIs rather than
     * /my_downloads URIs, for clients that have permission to do so.
     */
    void setAccessAllDownloads(boolean accessAllDownloads) {
        if (accessAllDownloads) {
            baseUri = downloadsUriProvider.getAllDownloadsUri();
        } else {
            baseUri = downloadsUriProvider.getContentUri();
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
        long insert = insert(request);
        DownloadJob.scheduleJob();
        return insert;
    }

    private long insert(Request request) {
        ContentValues values = request.toContentValues();
        Uri downloadUri = contentResolver.insert(downloadsUriProvider.getContentUri(), values);
        return ContentUris.parseId(downloadUri);
    }

    /**
     * {@link BatchPauseResumeController#pauseBatch(long)}
     */
    public boolean pauseBatch(long batchId) {
        boolean pauseBatch = batchPauseResumeController.pauseBatch(batchId);
        DownloadJob.scheduleJob();
        return pauseBatch;
    }

    /**
     * {@link BatchPauseResumeController#resumeBatch(long)}}
     */
    public boolean resumeBatch(long batchId) {
        boolean resumeBatch = batchPauseResumeController.resumeBatch(batchId);
        DownloadJob.scheduleJob();
        return resumeBatch;
    }

    public void removeDownload(URI uri) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    downloadsUriProvider.getContentUri(),
                    new String[]{"_id"},
                    DownloadContract.Downloads.COLUMN_FILE_NAME_HINT + "=?",
                    new String[]{uri.toString()},
                    null
            );
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                removeDownloads(id);
                return;
            }
            LLog.e("Didn't delete anything for uri: " + uri);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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
        values.put(DownloadContract.Downloads.COLUMN_DELETED, 1);
        // if only one id is passed in, then include it in the uri itself.
        // this will eliminate a full database scan in the download service.
        if (ids.length == 1) {
            return contentResolver.update(ContentUris.withAppendedId(baseUri, ids[0]), values, null, null);
        }
        return contentResolver.update(baseUri, values, getWhereClauseFor(ids, DownloadContract.Downloads._ID), longArrayToStringArray(ids));
    }

    /**
     * Cancel batch downloads and remove them from the download manager.  Each download will be stopped if
     * it was running, and it will no longer be accessible through the download manager.
     * If there are any downloaded files, partial or complete, they will be deleted.
     *
     * @param batchIds the IDs of the batches to remove
     * @return the number of batches actually removed
     */
    public int removeBatches(long... batchIds) {
        LLog.v("delete");
        if (batchIds == null || batchIds.length == 0) {
            throw new IllegalArgumentException("called with nothing to remove. input param 'batchIds' can't be null");
        }

        setDeletingStatusFor(batchIds);
        int batchesToBeDeleted = markBatchesToBeDeleted(batchIds);
        DownloadJob.scheduleEnforcedJob();
        return batchesToBeDeleted;
    }

    private void setDeletingStatusFor(long[] batchesIds) {
        ContentValues values = new ContentValues(1);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.DELETING);

        if (batchesIds.length == 1) {
            contentResolver.update(downloadsUriProvider.getContentUri(), values, COLUMN_BATCH_ID + "=?", new String[]{String.valueOf(batchesIds[0])});
        } else {
            contentResolver.update(downloadsUriProvider.getContentUri(), values, getWhereClauseFor(batchesIds, COLUMN_BATCH_ID), longArrayToStringArray(batchesIds));
        }
    }

    private int markBatchesToBeDeleted(long[] batchesIds) {
        ContentValues valuesDelete = new ContentValues(1);
        valuesDelete.put(DownloadContract.Batches.COLUMN_DELETED, 1);

        if (batchesIds.length == 1) {
            return contentResolver.update(ContentUris.withAppendedId(downloadsUriProvider.getBatchesUri(), batchesIds[0]), valuesDelete, null, null);
        }

        return contentResolver.update(
                downloadsUriProvider.getBatchesUri(),
                valuesDelete,
                getWhereClauseFor(batchesIds, DownloadContract.Downloads._ID),
                longArrayToStringArray(batchesIds)
        );
    }

    /**
     * Query the download manager about downloads that have been requested.
     *
     * @param query parameters specifying filters for this query
     * @return a Cursor over the result set of downloads, with columns consisting of all the
     * COLUMN_* constants.
     */
    public Cursor query(Query query) {
        Cursor underlyingCursor = query.runQuery(contentResolver, UNDERLYING_COLUMNS, downloadsUriProvider.getDownloadsByBatchUri());
        if (underlyingCursor == null) {
            return null;
        }
        PublicFacingStatusTranslator statusTranslator = new PublicFacingStatusTranslator();
        return new CursorTranslator(underlyingCursor, downloadsUriProvider.getDownloadsByBatchUri(), statusTranslator);
    }

    /**
     * Query the download manager about batches that have been requested.
     *
     * @param query parameters specifying filters for this query
     * @return a Cursor over the result set of batches
     */
    public Cursor query(BatchQuery query) {
        DownloadDeleter downloadDeleter = new DownloadDeleter(contentResolver);
        RealSystemFacade systemFacade = new RealSystemFacade(GlobalState.getContext(), new Clock());
        BatchRepository batchRepository = BatchRepository.from(contentResolver, downloadDeleter, downloadsUriProvider, systemFacade);
        Cursor cursor = batchRepository.retrieveFor(query);
        if (cursor == null) {
            return null;
        }

        PublicFacingStatusTranslator statusTranslator = new PublicFacingStatusTranslator();
        return new CursorTranslator(cursor, downloadsUriProvider.getBatchesUri(), statusTranslator);
    }

    /**
     * Open a downloaded file for reading.  The download must have completed.
     *
     * @param id the ID of the download
     * @return a read-only {@link ParcelFileDescriptor}
     * @throws FileNotFoundException if the destination file does not already exist
     */
    public ParcelFileDescriptor openDownloadedFile(long id) throws FileNotFoundException {
        return contentResolver.openFileDescriptor(getDownloadUri(id), "r");
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
                            DownloadContract.Downloads.COLUMN_DESTINATION
                    );
                    int destination = cursor.getInt(indx);
                    // TODO: if we ever add API to DownloadManager to let the caller specify
                    // non-external storage for a downloaded file, then the following code
                    // should also check for that destination.
                    if (destination == DownloadsDestination.DESTINATION_CACHE_PARTITION ||
                            destination == DownloadsDestination.DESTINATION_SYSTEMCACHE_PARTITION ||
                            destination == DownloadsDestination.DESTINATION_CACHE_PARTITION_NOROAMING ||
                            destination == DownloadsDestination.DESTINATION_CACHE_PARTITION_PURGEABLE) {
                        // return private uri
                        return ContentUris.withAppendedId(downloadsUriProvider.getContentUri(), id);
                    } else {
                        // return public uri
                        String path = cursor.getString(
                                cursor.getColumnIndexOrThrow(COLUMN_LOCAL_FILENAME)
                        );
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
            if (cursor.moveToFirst()) {
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
                                    + cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                    );
                }
            }
        } finally {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(DownloadContract.Downloads.COLUMN_CURRENT_BYTES, 0);
        values.put(DownloadContract.Downloads.COLUMN_TOTAL_BYTES, -1);
        values.putNull(DownloadContract.Downloads.COLUMN_DATA);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.PENDING);
        values.put(DownloadContract.Downloads.COLUMN_FAILED_CONNECTIONS, 0);
        contentResolver.update(baseUri, values, getWhereClauseFor(ids, DownloadContract.Downloads._ID), longArrayToStringArray(ids));
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
     * Adds a file to the downloads database system.
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
                .setNotificationVisibility(showNotification ? NotificationVisibility.ONLY_WHEN_COMPLETE : NotificationVisibility.HIDDEN);

        if (isMediaScannerScannable) {
            request.allowScanningByMediaScanner();
        }

        return addCompletedBatch(request.asBatch());
    }

    /**
     * Adds this batch to the downloads system.
     *
     * @param requestBatch A batch of completed downloads which have been downloaded not using this DownloadManager
     * @return the id of the batch which can be used to query the download manager
     */
    public long addCompletedBatch(RequestBatch requestBatch) {
        long completedBatchId = insertBatchAsCompleted(requestBatch);
        for (Request request : requestBatch.getRequests()) {
            request.setBatchId(completedBatchId);
            File file = new File(request.getDestinationPath());
            long length = file.exists() ? file.length() : 0;
            insertRequestAsCompletedDownload(request.getDestinationPath(), length, request);
        }
        return completedBatchId;
    }

    private long insertBatchAsCompleted(RequestBatch batch) {
        ContentValues values = batch.toContentValues();
        values.put(DownloadContract.Batches.COLUMN_STATUS, DownloadStatus.SUCCESS);
        values.put(DownloadContract.Batches.COLUMN_LAST_MODIFICATION, systemFacade.currentTimeMillis());
        Uri batchUri = contentResolver.insert(downloadsUriProvider.getBatchesUri(), values);
        return ContentUris.parseId(batchUri);
    }

    private long insertRequestAsCompletedDownload(String path, long length, Request request) {
        ContentValues values = request.toContentValues();
        values.put(DownloadContract.Downloads.COLUMN_DESTINATION, DownloadsDestination.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD);
        values.put(DownloadContract.Downloads.COLUMN_DATA, path);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.SUCCESS);
        values.put(DownloadContract.Downloads.COLUMN_TOTAL_BYTES, length);
        values.put(DownloadContract.Downloads.COLUMN_CURRENT_BYTES, length);
        Uri downloadUri = contentResolver.insert(downloadsUriProvider.getContentUri(), values);
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
        return ContentUris.withAppendedId(baseUri, id);
    }

    /**
     * This is the uri for the underlying table
     * use this at your own risk as many of the constants defined here will not return you what you expect for raw cursor data
     */
    public Uri getContentUri() {
        return downloadsUriProvider.getContentUri();
    }

    /**
     * Uri for the batches table
     */
    public Uri getBatchesUri() {
        return downloadsUriProvider.getBatchesUri();
    }

    static String getWhereClauseFor(long[] ids, String column) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                whereClause.append("OR ");
            }
            whereClause.append(column);
            whereClause.append(" = ? ");
        }
        whereClause.append(")");
        return whereClause.toString();
    }

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
        notifyBatchesHaveChanged();
        DownloadJob.scheduleJob();

        return batchId;
    }

    private void notifyBatchesHaveChanged() {
        contentResolver.notifyChange(getBatchesUri(), null);
        contentResolver.notifyChange(getBatchesWithoutProgressUri(), null);
    }

    private long insert(RequestBatch batch) {
        ContentValues values = batch.toContentValues();
        values.put(DownloadContract.Batches.COLUMN_STATUS, DownloadStatus.PENDING);
        values.put(DownloadContract.Batches.COLUMN_LAST_MODIFICATION, systemFacade.currentTimeMillis());
        Uri batchUri = contentResolver.insert(downloadsUriProvider.getBatchesUri(), values);
        return ContentUris.parseId(batchUri);
    }

    public Uri getDownloadsWithoutProgressUri() {
        return downloadsUriProvider.getDownloadsWithoutProgressUri();
    }

    public Uri getBatchesWithoutProgressUri() {
        return downloadsUriProvider.getBatchesWithoutProgressUri();
    }

    /**
     * Restart will activate the downloads workflow in case that it was not already active
     * <p/>
     * A possible scenario: A client denies a download for a particular business rule and that
     * rule does not apply any more. Calling this method will reactivate the downloads workflow,
     * check the client rules and proceed if necessary
     * <p/>
     * This method can be called as many times as desired as the system will take care that only
     * one instance is running, ignoring further calls if is currently active
     */
    public void forceStart() {
        DownloadJob.scheduleJob();
    }
}
