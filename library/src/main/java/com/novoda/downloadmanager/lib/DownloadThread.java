/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.drm.DrmManagerClient;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Pair;

import com.novoda.notils.logger.simple.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Locale;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.novoda.downloadmanager.lib.DownloadStatus.HTTP_DATA_ERROR;
import static com.novoda.downloadmanager.lib.FileDownloadInfo.NetworkState;
import static java.net.HttpURLConnection.*;

/**
 * Task which executes a given {@link FileDownloadInfo}: making network requests,
 * persisting data to disk, and updating {@link DownloadProvider}.
 */
class DownloadThread implements Runnable {

    /**
     * For intents used to notify the user that a download exceeds a size threshold, if this extra
     * is true, WiFi is required for this download size; otherwise, it is only recommended.
     */
    public static final String EXTRA_IS_WIFI_REQUIRED = "isWifiRequired";

    private static final String TAG = "DownloadManager-DownloadThread";

    // TODO: bind each download to a specific network interface to avoid state
    // checking races once we have ConnectivityManager API

    private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    private static final int HTTP_TEMP_REDIRECT = 307;

    private static final int DEFAULT_TIMEOUT = (int) (20 * SECOND_IN_MILLIS);

    private final Context context;
    private final FileDownloadInfo originalDownloadInfo;
    private final SystemFacade systemFacade;
    private final StorageManager storageManager;
    private final DownloadNotifier downloadNotifier;
    private final BatchCompletionBroadcaster batchCompletionBroadcaster;
    private final BatchRepository batchRepository;
    private final DownloadsUriProvider downloadsUriProvider;
    private final DownloadsRepository downloadsRepository;
    private final NetworkChecker networkChecker;
    private final DownloadReadyChecker downloadReadyChecker;

    private volatile boolean policyDirty;

    public DownloadThread(Context context,
                          SystemFacade systemFacade,
                          FileDownloadInfo originalDownloadInfo,
                          StorageManager storageManager,
                          DownloadNotifier downloadNotifier,
                          BatchCompletionBroadcaster batchCompletionBroadcaster,
                          BatchRepository batchRepository,
                          DownloadsUriProvider downloadsUriProvider,
                          DownloadsRepository downloadsRepository,
                          NetworkChecker networkChecker,
                          DownloadReadyChecker downloadReadyChecker) {
        this.context = context;
        this.systemFacade = systemFacade;
        this.originalDownloadInfo = originalDownloadInfo;
        this.storageManager = storageManager;
        this.downloadNotifier = downloadNotifier;
        this.batchCompletionBroadcaster = batchCompletionBroadcaster;
        this.batchRepository = batchRepository;
        this.downloadsUriProvider = downloadsUriProvider;
        this.downloadsRepository = downloadsRepository;
        this.networkChecker = networkChecker;
        this.downloadReadyChecker = downloadReadyChecker;
    }

    /**
     * Returns the user agent provided by the initiating app, or use the default one
     */
    private String userAgent() {
        String userAgent = originalDownloadInfo.getUserAgent();
        if (userAgent == null) {
            userAgent = Constants.DEFAULT_USER_AGENT;
        }
        return userAgent;
    }

    /**
     * State for the entire run() method.
     */
    static class State {
        public String filename;
        public String mimeType;
        public int retryAfter = 0;
        public boolean gotData = false;
        public String requestUri;
        public long totalBytes = -1;
        public long currentBytes = 0;
        public String headerETag;
        public boolean continuingDownload = false;
        public long bytesNotified = 0;
        public long timeLastNotification = 0;
        public int networkType = -1; //ConnectivityManager.TYPE_NONE;

        /**
         * Historical bytes/second speed of this download.
         */
        public long speed;
        /**
         * Time when current sample started.
         */
        public long speedSampleStart;
        /**
         * Bytes transferred since current sample started.
         */
        public long speedSampleBytes;

        public long contentLength = -1;
        public String contentDisposition;
        public String contentLocation;

        public int redirectionCount;
        public URL url;

        public State(FileDownloadInfo info) {
            mimeType = normalizeMimeType(info.getMimeType());
            requestUri = info.getUri();
            filename = info.getFileName();
            totalBytes = info.getTotalBytes();
            currentBytes = info.getCurrentBytes();
        }

        public void resetBeforeExecute() {
            // Reset any state from previous execution
            contentLength = -1;
            contentDisposition = null;
            contentLocation = null;
            redirectionCount = 0;
        }
    }

    private static String normalizeMimeType(String type) {
        if (type == null) {
            return null;
        }

        type = type.trim().toLowerCase(Locale.ROOT);

        final int semicolonIndex = type.indexOf(';');
        if (semicolonIndex != -1) {
            type = type.substring(0, semicolonIndex);
        }
        return type;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        try {
            runInternal();
        } finally {
            downloadNotifier.notifyDownloadSpeed(originalDownloadInfo.getId(), 0);
        }
    }

    private void runInternal() {
        // Skip when download already marked as finished; this download was probably started again while racing with UpdateThread.
        int downloadStatus = FileDownloadInfo.queryDownloadStatus(getContentResolver(), originalDownloadInfo.getId(), downloadsUriProvider);
        if (downloadStatus == DownloadStatus.SUCCESS) {
            Log.d("Download " + originalDownloadInfo.getId() + " already finished; skipping");
            return;
        }
        if (DownloadStatus.isCancelled(downloadStatus)) {
            Log.d("Download " + originalDownloadInfo.getId() + " already cancelled; skipping");
            return;
        }
        if (DownloadStatus.isError(downloadStatus)) {
            Log.d("Download " + originalDownloadInfo.getId() + " already failed: status = " + downloadStatus + "; skipping");
            return;
        }
        if (DownloadStatus.isDeleting(downloadStatus)) {
            Log.d("Download " + originalDownloadInfo.getId() + " is deleting: status = " + downloadStatus + "; skipping");
            return;
        }

        DownloadBatch currentBatch = batchRepository.retrieveBatchFor(originalDownloadInfo);

        if (!downloadReadyChecker.canDownload(currentBatch)) {
            Log.d("Download " + originalDownloadInfo.getId() + " is not ready to download: skipping");
            return;
        }

        if (downloadStatus != DownloadStatus.RUNNING) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.RUNNING);
            context.getContentResolver().update(originalDownloadInfo.getAllDownloadsUri(), contentValues, null, null);
            updateBatchStatus(originalDownloadInfo.getBatchId(), originalDownloadInfo.getId());
        }

        State state = new State(originalDownloadInfo);
        PowerManager.WakeLock wakeLock = null;
        int finalStatus = DownloadStatus.UNKNOWN_ERROR;
        int numFailed = originalDownloadInfo.getNumFailed();
        String errorMsg = null;

//        final NetworkPolicyManager netPolicy = NetworkPolicyManager.from(context);
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        try {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            wakeLock.acquire();

            // while performing download, register for rules updates
//            netPolicy.registerListener(mPolicyListener);

            Log.i("Download " + originalDownloadInfo.getId() + " starting");

            // Remember which network this download started on; used to
            // determine if errors were due to network changes.
            final NetworkInfo networkInfo = systemFacade.getActiveNetworkInfo(); // Param downloadInfo.uid removed TODO
            if (networkInfo != null) {
                state.networkType = networkInfo.getType();
            }

            // Network traffic on this thread should be counted against the
            // requesting UID, and is tagged with well-known value.
            TrafficStats.setThreadStatsTag(0xFFFFFF01); // TrafficStats.TAG_SYSTEM_DOWNLOAD
//            TrafficStats.setThreadStatsUid(downloadInfo.uid); Won't need this as we will be an Android library (doing own work)

            try {
                // TODO: migrate URL sanity checking into client side of API
                state.url = new URL(state.requestUri);
            } catch (MalformedURLException e) {
                throw new StopRequestException(DownloadStatus.BAD_REQUEST, e);
            }

            executeDownload(state);

            finalizeDestinationFile(state);
            finalStatus = DownloadStatus.SUCCESS;
        } catch (StopRequestException error) {
            // remove the cause before printing, in case it contains PII
            errorMsg = error.getMessage();
            String msg = "Aborting request for download " + originalDownloadInfo.getId() + ": " + errorMsg;
            Log.w(msg, error);
            finalStatus = error.getFinalStatus();

            // Nobody below our level should request retries, since we handle
            // failure counts at this level.
            if (finalStatus == DownloadStatus.WAITING_TO_RETRY) {
                throw new IllegalStateException("Execution should always throw final error codes");
            }

            // Some errors should be retryable, unless we fail too many times.
            if (isStatusRetryable(finalStatus)) {
                if (state.gotData) {
                    numFailed = 1;
                } else {
                    numFailed += 1;
                }

                if (numFailed < Constants.MAX_RETRIES) {
                    final NetworkInfo info = systemFacade.getActiveNetworkInfo(); // Param downloadInfo.uid removed TODO
                    if (info != null && info.getType() == state.networkType && info.isConnected()) {
                        // Underlying network is still intact, use normal backoff
                        finalStatus = DownloadStatus.WAITING_TO_RETRY;
                    } else {
                        // Network changed, retry on any next available
                        finalStatus = DownloadStatus.WAITING_FOR_NETWORK;
                    }
                }
            }

            // fall through to finally block
        } catch (Throwable ex) {
            errorMsg = ex.getMessage();
            String msg = "Exception for id " + originalDownloadInfo.getId() + ": " + errorMsg;
            Log.w(msg, ex);
            finalStatus = DownloadStatus.UNKNOWN_ERROR;
            // falls through to the code that reports an error
        } finally {
            TrafficStats.clearThreadStatsTag();
//            TrafficStats.clearThreadStatsUid();

            cleanupDestination(state, finalStatus);
            notifyDownloadCompleted(state, finalStatus, errorMsg, numFailed);

            Log.i("Download " + originalDownloadInfo.getId() + " finished with status " + DownloadStatus.statusToString(finalStatus));

//            netPolicy.unregisterListener(mPolicyListener);

            if (wakeLock != null) {
                wakeLock.release();
            }
        }
        storageManager.incrementNumDownloadsSoFar();
    }

    /**
     * Fully execute a single download request. Setup and send the request,
     * handle the response, and transfer the data to the destination file.
     */
    private void executeDownload(State state) throws StopRequestException {
        state.resetBeforeExecute();
        setupDestinationFile(state);

        // skip when already finished; remove after fixing race in 5217390
        if (state.currentBytes == state.totalBytes) {
            Log.i("Skipping initiating request for download " + originalDownloadInfo.getId() + "; already completed");
            return;
        }

        while (state.redirectionCount++ < Constants.MAX_REDIRECTS) {
            // Open connection and follow any redirects until we have a useful
            // response with body.
            HttpURLConnection conn = null;
            try {
                checkConnectivity();
                conn = (HttpURLConnection) state.url.openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(DEFAULT_TIMEOUT);
                conn.setReadTimeout(DEFAULT_TIMEOUT);

                addRequestHeaders(state, conn);

                final int responseCode = conn.getResponseCode();
                switch (responseCode) {
                    case HTTP_OK:
                        if (state.continuingDownload) {
                            throw new StopRequestException(DownloadStatus.CANNOT_RESUME, "Expected partial, but received OK");
                        }
                        processResponseHeaders(state, conn);
                        transferData(state, conn);
                        return;

                    case HTTP_PARTIAL:
                        if (!state.continuingDownload) {
                            throw new StopRequestException(DownloadStatus.CANNOT_RESUME, "Expected OK, but received partial");
                        }
                        transferData(state, conn);
                        return;

                    case HTTP_MOVED_PERM:
                    case HTTP_MOVED_TEMP:
                    case HTTP_SEE_OTHER:
                    case HTTP_TEMP_REDIRECT:
                        final String location = conn.getHeaderField("Location");
                        state.url = new URL(state.url, location);
                        if (responseCode == HTTP_MOVED_PERM) {
                            // Push updated URL back to database
                            state.requestUri = state.url.toString();
                        }
                        continue;

                    case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE:
                        throw new StopRequestException(DownloadStatus.CANNOT_RESUME, "Requested range not satisfiable");

                    case HTTP_UNAVAILABLE:
                        parseRetryAfterHeaders(state, conn);
                        throw new StopRequestException(
                                HTTP_UNAVAILABLE, conn.getResponseMessage());

                    case HTTP_INTERNAL_ERROR:
                        throw new StopRequestException(
                                HTTP_INTERNAL_ERROR, conn.getResponseMessage());

                    default:
                        StopRequestException.throwUnhandledHttpError(responseCode, conn.getResponseMessage());
                }
            } catch (UnknownHostException e) {
                // Unable to resolve host request
                throw new StopRequestException(HTTP_NOT_FOUND, e);
            } catch (IOException e) {
                // Trouble with low-level sockets
                throw new StopRequestException(HTTP_DATA_ERROR, e);

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        throw new StopRequestException(DownloadStatus.TOO_MANY_REDIRECTS, "Too many redirects");
    }

    /**
     * Transfer data from the given connection to the destination file.
     */
    private void transferData(State state, HttpURLConnection conn) throws StopRequestException {
        DrmManagerClient drmClient = null;
        InputStream in = null;
        OutputStream out = null;
        FileDescriptor outFd = null;
        try {
            try {
                in = conn.getInputStream();
            } catch (IOException e) {
                throw new StopRequestException(HTTP_DATA_ERROR, e);
            }

            try {
                if (DownloadDrmHelper.isDrmConvertNeeded(state.mimeType)) {
//                    drmClient = new DrmManagerClient(context);
//                    final RandomAccessFile file = new RandomAccessFile(new File(state.filename), "rw");
//                    out = new DrmOutputStream(drmClient, file, state.mimeType);
//                    outFd = file.getFD();
                    throw new IllegalStateException("DRM not supported atm");
                } else {
                    out = new FileOutputStream(state.filename, true);
                    outFd = ((FileOutputStream) out).getFD();
                }
            } catch (IOException e) {
                throw new StopRequestException(DownloadStatus.FILE_ERROR, e);
            }

            // Start streaming data, periodically watch for pause/cancel
            // commands and checking disk space as needed.
            transferData(state, in, out);

//            try {
//                if (out instanceof DrmOutputStream) {
//                    ((DrmOutputStream) out).finish();
//                }
//            } catch (IOException e) {
//                throw new StopRequestException(STATUS_FILE_ERROR, e);
//            }

        } finally {
//            if (drmClient != null) {
//                drmClient.release();
//            }

            closeQuietly(in);

            try {
                if (out != null) {
                    out.flush();
                }
                if (outFd != null) {
                    outFd.sync();
                }
            } catch (IOException e) {
                Log.e("Fail sync");
            } finally {
                closeQuietly(out);
            }
        }
    }

    /**
     * Check if current connectivity is valid for this request.
     */
    private void checkConnectivity() throws StopRequestException {
        // checking connectivity will apply current policy
        policyDirty = false;

        final NetworkState networkUsable = networkChecker.checkCanUseNetwork(originalDownloadInfo);
        if (networkUsable != NetworkState.OK) {
            int status = DownloadStatus.WAITING_FOR_NETWORK;
            if (networkUsable == NetworkState.UNUSABLE_DUE_TO_SIZE) {
                status = DownloadStatus.QUEUED_FOR_WIFI;
                notifyPauseDueToSize(true);
            } else if (networkUsable == NetworkState.RECOMMENDED_UNUSABLE_DUE_TO_SIZE) {
                status = DownloadStatus.QUEUED_FOR_WIFI;
                notifyPauseDueToSize(false);
            }
            throw new StopRequestException(status, networkUsable.name());
        }
    }

    void notifyPauseDueToSize(boolean isWifiRequired) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(originalDownloadInfo.getAllDownloadsUri());
        intent.setClassName(SizeLimitActivity.class.getPackage().getName(), SizeLimitActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_IS_WIFI_REQUIRED, isWifiRequired);
        context.startActivity(intent);
    }

    /**
     * Transfer as much data as possible from the HTTP response to the
     * destination file.
     */
    private void transferData(State state, InputStream in, OutputStream out) throws StopRequestException {
        final byte data[] = new byte[Constants.BUFFER_SIZE];
        for (; ; ) {
            int bytesRead = readFromResponse(state, data, in);
            if (bytesRead == -1) { // success, end of stream already reached
                handleEndOfStream(state);
                return;
            }

            state.gotData = true;
            writeDataToDestination(state, data, bytesRead, out);
            state.currentBytes += bytesRead;
            reportProgress(state);

//            Log.v("downloaded " + state.currentBytes + " for " + downloadInfo.uri);

            checkPausedOrCanceled();
        }
    }

    /**
     * Called after a successful completion to take any necessary action on the downloaded file.
     */
    private void finalizeDestinationFile(State state) {
        if (state.filename != null) {
            // make sure the file is readable
            setPermissions(state.filename, 0644, -1, -1);
            // FileUtils.setPermission
            //http://stackoverflow.com/questions/11408154/how-to-get-file-permission-mode-programmatically-in-java
        }
    }

    /**
     * Called just before the thread finishes, regardless of status, to take any necessary action on
     * the downloaded file.
     */
    private void cleanupDestination(State state, int finalStatus) {
        if (state.filename != null && DownloadStatus.isError(finalStatus)) {
            Log.d("cleanupDestination() deleting " + state.filename);
            boolean deleted = new File(state.filename).delete();
            if (!deleted) {
                Log.e("File not deleted");
            }
            state.filename = null;
        }
    }

    /**
     * Check if the download has been paused or canceled, stopping the request appropriately if it
     * has been.
     */
    private void checkPausedOrCanceled() throws StopRequestException {
        FileDownloadInfo.ControlStatus controlStatus = downloadsRepository.getDownloadInfoControlStatusFor(originalDownloadInfo.getId());

        if (controlStatus.isPaused()) {
            throw new StopRequestException(DownloadStatus.PAUSED_BY_APP, "download paused by owner");
        }
        if (controlStatus.isCanceled()) {
            throw new StopRequestException(DownloadStatus.CANCELED, "download canceled");
        }

        // if policy has been changed, trigger connectivity check
        if (policyDirty) {
            checkConnectivity();
        }
    }

    /**
     * Report download progress through the database if necessary.
     */
    private void reportProgress(State state) {
        final long now = SystemClock.elapsedRealtime();

        final long sampleDelta = now - state.speedSampleStart;
        if (sampleDelta > 500) {
            final long sampleSpeed = ((state.currentBytes - state.speedSampleBytes) * 1000) / sampleDelta;

            if (state.speed == 0) {
                state.speed = sampleSpeed;
            } else {
                state.speed = ((state.speed * 3) + sampleSpeed) / 4;
            }

            // Only notify once we have a full sample window
            if (state.speedSampleStart != 0) {
                downloadNotifier.notifyDownloadSpeed(originalDownloadInfo.getId(), state.speed);
            }

            state.speedSampleStart = now;
            state.speedSampleBytes = state.currentBytes;
        }

        if (state.currentBytes - state.bytesNotified > Constants.MIN_PROGRESS_STEP &&
                now - state.timeLastNotification > Constants.MIN_PROGRESS_TIME) {
            ContentValues values = new ContentValues();
            values.put(DownloadContract.Downloads.COLUMN_CURRENT_BYTES, state.currentBytes);
            getContentResolver().update(originalDownloadInfo.getAllDownloadsUri(), values, null, null);
            state.bytesNotified = state.currentBytes;
            state.timeLastNotification = now;
        }
    }

    /**
     * Write a data buffer to the destination file.
     *
     * @param data      buffer containing the data to write
     * @param bytesRead how many bytes to write from the buffer
     */
    private void writeDataToDestination(State state, byte[] data, int bytesRead, OutputStream out) throws StopRequestException {
        storageManager.verifySpaceBeforeWritingToFile(originalDownloadInfo.getDestination(), state.filename, bytesRead);

        boolean forceVerified = false;
        while (true) {
            try {
                out.write(data, 0, bytesRead);
                return;
            } catch (IOException ex) {
                // TODO: better differentiate between DRM and disk failures
                if (!forceVerified) {
                    // couldn't write to file. are we out of space? check.
                    storageManager.verifySpace(originalDownloadInfo.getDestination(), state.filename, bytesRead);
                    forceVerified = true;
                } else {
                    throw new StopRequestException(
                            DownloadStatus.FILE_ERROR,
                            "Failed to write data: " + ex);
                }
            }
        }
    }

    /**
     * Called when we've reached the end of the HTTP response stream, to update the database and
     * check for consistency.
     */
    private void handleEndOfStream(State state) throws StopRequestException {
        ContentValues values = new ContentValues(2);
        values.put(DownloadContract.Downloads.COLUMN_CURRENT_BYTES, state.currentBytes);
        if (state.contentLength == -1) {
            values.put(DownloadContract.Downloads.COLUMN_TOTAL_BYTES, state.currentBytes);
        }
        getContentResolver().update(originalDownloadInfo.getAllDownloadsUri(), values, null, null);

        final boolean lengthMismatched = (state.contentLength != -1)
                && (state.currentBytes != state.contentLength);
        if (lengthMismatched) {
            if (cannotResume(state)) {
                throw new StopRequestException(DownloadStatus.CANNOT_RESUME, "mismatched content length; unable to resume");
            } else {
                throw new StopRequestException(HTTP_DATA_ERROR, "closed socket before end of file");
            }
        }
    }

    private boolean cannotResume(State state) {
        return (state.currentBytes > 0 && !originalDownloadInfo.isResumable() || DownloadDrmHelper.isDrmConvertNeeded(state.mimeType));
    }

    /**
     * Read some data from the HTTP response stream, handling I/O errors.
     *
     * @param data         buffer to use to read data
     * @param entityStream stream for reading the HTTP response entity
     * @return the number of bytes actually read or -1 if the end of the stream has been reached
     */
    private int readFromResponse(State state, byte[] data, InputStream entityStream)
            throws StopRequestException {
        try {
            return entityStream.read(data);
        } catch (IOException ex) {
            // TODO: handle stream errors the same as other retries
            if ("unexpected end of stream".equals(ex.getMessage())) {
                return -1;
            }

            ContentValues values = new ContentValues(1);
            values.put(DownloadContract.Downloads.COLUMN_CURRENT_BYTES, state.currentBytes);
            getContentResolver().update(originalDownloadInfo.getAllDownloadsUri(), values, null, null);
            if (cannotResume(state)) {
                throw new StopRequestException(DownloadStatus.CANNOT_RESUME, "Failed reading response: " + ex + "; unable to resume", ex);
            } else {
                throw new StopRequestException(HTTP_DATA_ERROR, "Failed reading response: " + ex, ex);
            }
        }
    }

    /**
     * Prepare target file based on given network response. Derives filename and
     * target size as needed.
     */
    private void processResponseHeaders(State state, HttpURLConnection conn) throws StopRequestException {
        // TODO: fallocate the entire file if header gave us specific length

        readResponseHeaders(state, conn);

        state.filename = Helpers.generateSaveFile(
                originalDownloadInfo.getUri(),
                originalDownloadInfo.getHint(),
                state.contentDisposition,
                state.contentLocation,
                state.mimeType,
                originalDownloadInfo.getDestination(),
                state.contentLength,
                storageManager);

        updateDatabaseFromHeaders(state);
        // check connectivity again now that we know the total size
        checkConnectivity();
    }

    /**
     * Update necessary database fields based on values of HTTP response headers that have been
     * read.
     */
    private void updateDatabaseFromHeaders(State state) {
        ContentValues values = new ContentValues(4);
        values.put(DownloadContract.Downloads.COLUMN_DATA, state.filename);
        if (state.headerETag != null) {
            values.put(Constants.ETAG, state.headerETag);
        }
        if (state.mimeType != null) {
            values.put(DownloadContract.Downloads.COLUMN_MIME_TYPE, state.mimeType);
        }
        values.put(DownloadContract.Downloads.COLUMN_TOTAL_BYTES, originalDownloadInfo.getTotalBytes());
        getContentResolver().update(originalDownloadInfo.getAllDownloadsUri(), values, null, null);
    }

    /**
     * Read headers from the HTTP response and store them into local state.
     */
    private void readResponseHeaders(State state, HttpURLConnection conn) throws StopRequestException {
        state.contentDisposition = conn.getHeaderField("Content-Disposition");
        state.contentLocation = conn.getHeaderField("Content-Location");

        if (state.mimeType == null) {
            state.mimeType = normalizeMimeType(conn.getContentType());
        }

        state.headerETag = conn.getHeaderField("ETag");

        final String transferEncoding = conn.getHeaderField("Transfer-Encoding");
        if (transferEncoding == null) {
            state.contentLength = getHeaderFieldLong(conn, "Content-Length", -1);
        } else {
            Log.i("Ignoring Content-Length since Transfer-Encoding is also defined");
            state.contentLength = -1;
        }

        state.totalBytes = state.contentLength;
        originalDownloadInfo.setTotalBytes(state.contentLength);

        final boolean noSizeInfo = state.contentLength == -1 && (transferEncoding == null || !transferEncoding.equalsIgnoreCase("chunked"));
        if (!originalDownloadInfo.isNoIntegrity() && noSizeInfo) {
            throw new StopRequestException(DownloadStatus.CANNOT_RESUME, "can't know size of download, giving up");
        }
    }

    private void parseRetryAfterHeaders(State state, HttpURLConnection conn) {
        state.retryAfter = conn.getHeaderFieldInt("Retry-After", -1);
        if (state.retryAfter < 0) {
            state.retryAfter = 0;
        } else {
            if (state.retryAfter < Constants.MIN_RETRY_AFTER) {
                state.retryAfter = Constants.MIN_RETRY_AFTER;
            } else if (state.retryAfter > Constants.MAX_RETRY_AFTER) {
                state.retryAfter = Constants.MAX_RETRY_AFTER;
            }
            state.retryAfter += Helpers.sRandom.nextInt(Constants.MIN_RETRY_AFTER + 1);
            state.retryAfter *= 1000;
        }
    }

    /**
     * Prepare the destination file to receive data.  If the file already exists, we'll set up
     * appropriately for resumption.
     */
    private void setupDestinationFile(State state) throws StopRequestException {
        if (TextUtils.isEmpty(state.filename)) {
            // only true if we've already run a thread for this download
            return;
        }
        Log.i("have run thread before for id: " + originalDownloadInfo.getId() + ", and state.filename: " + state.filename);
        if (!Helpers.isFilenameValid(state.filename, storageManager.getDownloadDataDirectory())) {
            Log.d("Yeah we know we are bad for downloading to internal storage");
//                throw new StopRequestException(Downloads.Impl.STATUS_FILE_ERROR, "found invalid internal destination filename");
        }
        // We're resuming a download that got interrupted
        File destinationFile = new File(state.filename);
        if (destinationFile.exists()) {
            Log.i("resuming download for id: " + originalDownloadInfo.getId() + ", and state.filename: " + state.filename);
            long fileLength = destinationFile.length();
            if (fileLength == 0) {
                // The download hadn't actually started, we can restart from scratch
                Log.d("setupDestinationFile() found fileLength=0, deleting " + state.filename);
                destinationFile.delete();
                state.filename = null;
                Log.i("resuming download for id: " + originalDownloadInfo.getId() + ", BUT starting from scratch again: ");
            } else if (!originalDownloadInfo.isResumable()) {
                // This should've been caught upon failure
                Log.d("setupDestinationFile() unable to resume download, deleting " + state.filename);
                destinationFile.delete();
                throw new StopRequestException(DownloadStatus.CANNOT_RESUME, "Trying to resume a download that can't be resumed");
            } else {
                // All right, we'll be able to resume this download
                Log.i("resuming download for id: " + originalDownloadInfo.getId() + ", and starting with file of length: " + fileLength);
                state.currentBytes = (int) fileLength;
                if (originalDownloadInfo.getTotalBytes() != -1) {
                    state.contentLength = originalDownloadInfo.getTotalBytes();
                }
                state.headerETag = originalDownloadInfo.getETag();
                state.continuingDownload = true;
                Log.i("resuming download for id: " + originalDownloadInfo.getId() + ", state.currentBytes: " + state.currentBytes + ", and setting continuingDownload to true: ");
            }
        }
    }

    /**
     * Add custom headers for this download to the HTTP request.
     */
    private void addRequestHeaders(State state, HttpURLConnection conn) {
        for (Pair<String, String> header : originalDownloadInfo.getHeaders()) {
            conn.addRequestProperty(header.first, header.second);
        }

        // Only splice in user agent when not already defined
        if (conn.getRequestProperty("User-Agent") == null) {
            conn.addRequestProperty("User-Agent", userAgent());
        }

        // Defeat transparent gzip compression, since it doesn't allow us to
        // easily resume partial downloads.
        conn.setRequestProperty("Accept-Encoding", "identity");

        if (state.continuingDownload) {
            if (state.headerETag != null) {
                conn.addRequestProperty("If-Match", state.headerETag);
            }
            conn.addRequestProperty("Range", "bytes=" + state.currentBytes + "-");
        }
    }

    /**
     * Stores information about the completed download, and notifies the initiating application.
     */
    private void notifyDownloadCompleted(State state, int finalStatus, String errorMsg, int numFailed) {
        notifyThroughDatabase(state, finalStatus, errorMsg, numFailed);
        if (DownloadStatus.isCompleted(finalStatus)) {
            broadcastIntentDownloadComplete(finalStatus);
        } else if (DownloadStatus.isInsufficientSpace(finalStatus)) {
            broadcastIntentDownloadFailedInsufficientSpace();
        }
    }

    public void broadcastIntentDownloadComplete(int finalStatus) {
        Intent intent = new Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intent.setPackage(getPackageName());
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, originalDownloadInfo.getId());
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_STATUS, finalStatus);
        intent.setData(originalDownloadInfo.getMyDownloadsUri());
        if (originalDownloadInfo.getExtras() != null) {
            intent.putExtra(DownloadManager.EXTRA_EXTRA, originalDownloadInfo.getExtras());
        }
        context.sendBroadcast(intent);
    }

    public void broadcastIntentDownloadFailedInsufficientSpace() {
        Intent intent = new Intent(DownloadManager.ACTION_DOWNLOAD_INSUFFICIENT_SPACE);
        intent.setPackage(getPackageName());
        intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, originalDownloadInfo.getId());
        intent.setData(originalDownloadInfo.getMyDownloadsUri());
        if (originalDownloadInfo.getExtras() != null) {
            intent.putExtra(DownloadManager.EXTRA_EXTRA, originalDownloadInfo.getExtras());
        }
        context.sendBroadcast(intent);
    }

    private String getPackageName() {
        return context.getApplicationContext().getPackageName();
    }

    private void notifyThroughDatabase(State state, int finalStatus, String errorMsg, int numFailed) {
        ContentValues values = new ContentValues(8);
        values.put(DownloadContract.Downloads.COLUMN_STATUS, finalStatus);
        values.put(DownloadContract.Downloads.COLUMN_DATA, state.filename);
        values.put(DownloadContract.Downloads.COLUMN_MIME_TYPE, state.mimeType);
        values.put(DownloadContract.Downloads.COLUMN_LAST_MODIFICATION, systemFacade.currentTimeMillis());
        values.put(DownloadContract.Downloads.COLUMN_FAILED_CONNECTIONS, numFailed);
        values.put(Constants.RETRY_AFTER_X_REDIRECT_COUNT, state.retryAfter);

        if (!TextUtils.equals(originalDownloadInfo.getUri(), state.requestUri)) {
            values.put(DownloadContract.Downloads.COLUMN_URI, state.requestUri);
        }

        // save the error message. could be useful to developers.
        if (!TextUtils.isEmpty(errorMsg)) {
            values.put(DownloadContract.Downloads.COLUMN_ERROR_MSG, errorMsg);
        }
        getContentResolver().update(originalDownloadInfo.getAllDownloadsUri(), values, null, null);

        updateBatchStatus(originalDownloadInfo.getBatchId(), originalDownloadInfo.getId());
    }

    private ContentResolver getContentResolver() {
        return context.getContentResolver();
    }

    private void updateBatchStatus(long batchId, long downloadId) {
        int batchStatus = batchRepository.getBatchStatus(batchId);

        batchRepository.updateBatchStatus(batchId, batchStatus);

        if (DownloadStatus.isCancelled(batchStatus)) {
            ContentValues values = new ContentValues(1);
            values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.CANCELED);
            getContentResolver().update(downloadsUriProvider.getAllDownloadsUri(), values, DownloadContract.Downloads.COLUMN_BATCH_ID + " = ?", new String[]{String.valueOf(batchId)});
        } else if (DownloadStatus.isError(batchStatus)) {
            ContentValues values = new ContentValues(1);
            values.put(DownloadContract.Downloads.COLUMN_STATUS, DownloadStatus.BATCH_FAILED);
            getContentResolver().update(
                    downloadsUriProvider.getAllDownloadsUri(),
                    values,
                    DownloadContract.Downloads.COLUMN_BATCH_ID + " = ? AND " + DownloadContract.Downloads._ID + " <> ? ",
                    new String[]{String.valueOf(batchId), String.valueOf(downloadId)}
            );
        } else if (DownloadStatus.isSuccess(batchStatus)) {
            batchCompletionBroadcaster.notifyBatchCompletedFor(batchId);
        }
    }

    private static long getHeaderFieldLong(URLConnection conn, String field, long defaultValue) {
        try {
            return Long.parseLong(conn.getHeaderField(field));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Return if given status is eligible to be treated as
     * {@link DownloadStatus#WAITING_TO_RETRY}.
     */
    private static boolean isStatusRetryable(int status) {
        switch (status) {
            case HTTP_DATA_ERROR:
            case HTTP_UNAVAILABLE:
            case HTTP_INTERNAL_ERROR:
                return true;
            default:
                return false;
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    private void setPermissions(String fileName, int mode, int uid, int gid) {
        try {
            Class<?> fileUtils = Class.forName("android.os.FileUtils");
            Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
            setPermissions.invoke(null, fileName, mode, uid, gid);
        } catch (Exception e) {
            Log.e("Failed to set permissions. Unknown future behaviour.");
        }
    }
}
