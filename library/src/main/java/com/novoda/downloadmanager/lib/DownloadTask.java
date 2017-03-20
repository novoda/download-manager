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
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Pair;

import com.novoda.downloadmanager.lib.logger.LLog;

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
import static com.novoda.downloadmanager.lib.Constants.UNKNOWN_BYTE_SIZE;
import static com.novoda.downloadmanager.lib.DownloadStatus.HTTP_DATA_ERROR;
import static com.novoda.downloadmanager.lib.DownloadStatus.QUEUED_DUE_CLIENT_RESTRICTIONS;
import static com.novoda.downloadmanager.lib.FileDownloadInfo.NetworkState;
import static com.novoda.downloadmanager.lib.IOHelpers.closeAfterWrite;
import static java.net.HttpURLConnection.*;

/**
 * Task which executes a given {@link FileDownloadInfo}: making network requests,
 * persisting data to disk, and updating {@link DownloadProvider}.
 */
class DownloadTask {

    /**
     * For intents used to notify the user that a download exceeds a size threshold, if this extra
     * is true, WiFi is required for this download size; otherwise, it is only recommended.
     */
    static final String EXTRA_IS_WIFI_REQUIRED = "isWifiRequired";

    // TODO: bind each download to a specific network interface to avoid state
    // checking races once we have ConnectivityManager API

    private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    private static final int HTTP_TEMP_REDIRECT = 307;

    private static final int DEFAULT_TIMEOUT = (int) (20 * SECOND_IN_MILLIS);

    private final Context context;
    private final FileDownloadInfo originalDownloadInfo;
    private final DownloadBatch originalDownloadBatch;
    private final SystemFacade systemFacade;
    private final StorageManager storageManager;
    private final NotificationsUpdater notificationsUpdater;
    private final BatchInformationBroadcaster batchInformationBroadcaster;
    private final BatchRepository batchRepository;
    private final FileDownloadInfo.ControlStatus.Reader controlReader;
    private final NetworkChecker networkChecker;
    private final DownloadReadyChecker downloadReadyChecker;
    private final Clock clock;
    private final DownloadsRepository downloadsRepository;

    @SuppressWarnings("checkstyle:parameternumber")
    public DownloadTask(Context context,
                        SystemFacade systemFacade,
                        FileDownloadInfo originalDownloadInfo,
                        DownloadBatch originalDownloadBatch,
                        StorageManager storageManager,
                        NotificationsUpdater notificationsUpdater,
                        BatchInformationBroadcaster batchInformationBroadcaster,
                        BatchRepository batchRepository,
                        FileDownloadInfo.ControlStatus.Reader controlReader,
                        NetworkChecker networkChecker,
                        DownloadReadyChecker downloadReadyChecker,
                        Clock clock,
                        DownloadsRepository downloadsRepository) {
        this.context = context;
        this.systemFacade = systemFacade;
        this.originalDownloadInfo = originalDownloadInfo;
        this.originalDownloadBatch = originalDownloadBatch;
        this.storageManager = storageManager;
        this.notificationsUpdater = notificationsUpdater;
        this.batchInformationBroadcaster = batchInformationBroadcaster;
        this.batchRepository = batchRepository;
        this.controlReader = controlReader;
        this.networkChecker = networkChecker;
        this.downloadReadyChecker = downloadReadyChecker;
        this.clock = clock;
        this.downloadsRepository = downloadsRepository;
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
        public long totalBytes = UNKNOWN_BYTE_SIZE;
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

        public long contentLength = UNKNOWN_BYTE_SIZE;
        public String contentDisposition;
        public String contentLocation;

        public int redirectionCount;
        public URL url;
        public boolean shouldPause;

        public State(FileDownloadInfo info) {
            mimeType = normalizeMimeType(info.getMimeType());
            requestUri = info.getUri();
            filename = info.getFileName();
            totalBytes = info.getTotalBytes();
            currentBytes = info.getCurrentBytes();
        }

        State() {
            // This constructor is intentionally empty. Only used for tests.
        }

        public void resetBeforeExecute() {
            // Reset any state from previous execution
            contentLength = UNKNOWN_BYTE_SIZE;
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

    public void run() {
        try {
            runInternal();
        } finally {
            notificationsUpdater.updateDownloadSpeed(originalDownloadInfo.getId(), 0);
        }
    }

    private void runInternal() {
        // Skip when download already marked as finished; this download was probably started again while racing with UpdateThread.
        int downloadStatus = downloadsRepository.getDownloadStatus(originalDownloadInfo.getId());
        if (downloadStatus == DownloadStatus.SUCCESS) {
            LLog.d("Download " + originalDownloadInfo.getId() + " already finished; skipping");
            return;
        }
        if (DownloadStatus.isCancelled(downloadStatus)) {
            LLog.d("Download " + originalDownloadInfo.getId() + " already cancelled; skipping");
            return;
        }
        if (DownloadStatus.isError(downloadStatus)) {
            LLog.d("Download " + originalDownloadInfo.getId() + " already failed: status = " + downloadStatus + "; skipping");
            return;
        }
        if (DownloadStatus.isDeleting(downloadStatus)) {
            LLog.d("Download " + originalDownloadInfo.getId() + " is deleting: status = " + downloadStatus + "; skipping");
            return;
        }

        int finalStatus = DownloadStatus.UNKNOWN_ERROR;
        int numFailed = originalDownloadInfo.getNumFailed();
        String errorMsg = null;
        State state = new State(originalDownloadInfo);

        try {
            LLog.i("Download " + originalDownloadInfo.getId() + " starting");

            checkDownloadCanProceed();

            if (downloadStatus != DownloadStatus.RUNNING) {
                downloadsRepository.setDownloadRunning(originalDownloadInfo);
                updateBatchStatus(originalDownloadInfo.getBatchId(), originalDownloadInfo.getId());
            }

            // Remember which network this download started on; used to
            // determine if errors were due to network changes.
            final NetworkInfo networkInfo = systemFacade.getActiveNetworkInfo(); // Param downloadInfo.uid removed TODO
            if (networkInfo != null) {
                state.networkType = networkInfo.getType();
            }

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
            LLog.w(msg, error);
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

                if (numFailed < Constants.MAX_RETRIES && finalStatus != QUEUED_DUE_CLIENT_RESTRICTIONS) {
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
            LLog.w(msg, ex);
            finalStatus = DownloadStatus.UNKNOWN_ERROR;
            // falls through to the code that reports an error
        } finally {
            cleanupDestination(state, finalStatus);

            notifyDownloadCompleted(state, finalStatus, errorMsg, numFailed);
            hackToForceClientsRefreshRulesIfConnectionDropped(finalStatus);

            LLog.i("Download " + originalDownloadInfo.getId() + " finished with status " + DownloadStatus.statusToString(finalStatus));
        }
        storageManager.incrementNumDownloadsSoFar();
    }

    private void hackToForceClientsRefreshRulesIfConnectionDropped(int finalStatus) {
        if (DownloadStatus.WAITING_FOR_NETWORK == finalStatus) {
            try {
                checkClientRules();
            } catch (StopRequestException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fully execute a single download request. Setup and send the request,
     * handle the response, and transfer the data to the destination file.
     */
    private void executeDownload(State state) throws StopRequestException {
        state.resetBeforeExecute();
        setupDestinationFile(state);

        if (originalDownloadInfo.shouldAllowTarUpdate(state.mimeType)) {
            state.totalBytes = UNKNOWN_BYTE_SIZE;
        }

        // skip when already finished; remove after fixing race in 5217390
        if (downloadAlreadyFinished(state)) {
            LLog.i("Skipping initiating request for download " + originalDownloadInfo.getId() + "; already completed");
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
     * Check if the download has been paused or canceled, stopping the request appropriately if it
     * has been.
     */
    private void checkDownloadCanProceed() throws StopRequestException {
        LLog.v("checkDownloadCanProceed");

        checkIsPausedOrCanceled();

        checkClientRules();
    }

    private void checkIsPausedOrCanceled() throws StopRequestException {
        FileDownloadInfo.ControlStatus controlStatus = controlReader.newControlStatus();

        if (controlStatus.isDeleted()) {
            LLog.v("this is marked for deletion so we immediately stop downloading");
            throw new StopRequestException(DownloadStatus.CANCELED, "download deleted");
        }

        if (controlStatus.isPaused()) {
            throw new StopRequestException(DownloadStatus.PAUSED_BY_APP, "download paused by owner");
        }
        if (controlStatus.isCanceled()) {
            throw new StopRequestException(DownloadStatus.CANCELED, "download canceled");
        }
    }

    private void checkClientRules() throws StopRequestException {
        if (!downloadReadyChecker.clientAllowsToDownload(originalDownloadBatch)) {
            throw new StopRequestException(DownloadStatus.QUEUED_DUE_CLIENT_RESTRICTIONS, "Cannot proceed because client denies");
        }
    }

    private boolean downloadAlreadyFinished(State state) {
        return (state.currentBytes == state.totalBytes) && !originalDownloadInfo.shouldAllowTarUpdate(state.mimeType);
    }

    /**
     * Transfer data from the given connection to the destination file.
     */
    private void transferData(State state, HttpURLConnection conn) throws StopRequestException {
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

        } catch (StopRequestException exception) {
            if (exception.getFinalStatus() == DownloadStatus.PAUSED_BY_APP) {
                notifyThroughDatabase(state, DownloadStatus.PAUSING, exception.getMessage(), 0);
            }
            // We still have to throw the exception, otherwise the parent
            // thinks that the download has been completed OK, when is not
            // We should remove exceptions as a flow control in order to avoid this
            throw exception;
        } finally {
            closeQuietly(in);

            closeAfterWrite(out, outFd);
        }
    }

    /**
     * Check if current connectivity is valid for this request.
     */
    private void checkConnectivity() throws StopRequestException {
        // checking connectivity will apply current policy

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
        LLog.v("start transfer data");
        StorageSpaceVerifier spaceVerifier = new StorageSpaceVerifier(storageManager, originalDownloadInfo.getDestination(), state.filename);
        DataWriter checkedWriter = new CheckedWriter(spaceVerifier, out);
        DataWriter dataWriter = new NotifierWriter(
                getContentResolver(),
                checkedWriter,
                notificationsUpdater,
                originalDownloadInfo,
                checkOnWrite
        );

        DataTransferer dataTransferer;
        if (originalDownloadInfo.shouldAllowTarUpdate(state.mimeType)) {
            dataTransferer = new TarTruncator(dataWriter);
        } else {
            dataTransferer = new RegularDataTransferer(dataWriter);
        }

        State newState = dataTransferer.transferData(state, in);
        handleEndOfStream(newState);
        LLog.v("end transfer data");
    }

    private final NotifierWriter.WriteChunkListener checkOnWrite = new NotifierWriter.WriteChunkListener() {
        @Override
        public void chunkWritten(FileDownloadInfo downloadInfo) throws StopRequestException {
            if (clock.intervalLessThan(Clock.Interval.ONE_SECOND)) {
                return;
            }

            clock.startInterval();
            checkDownloadCanProceed();
        }
    };

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
            LLog.d("cleanupDestination() deleting " + state.filename);
            boolean deleted = new File(state.filename).delete();
            if (!deleted) {
                LLog.e("File not deleted");
            }
            state.filename = null;
        }
    }

    /**
     * Called when we've reached the end of the HTTP response stream, to update the database and
     * check for consistency.
     */
    private void handleEndOfStream(State state) throws StopRequestException {
        if (state.shouldPause) {
            updateStatusAndPause(state);
            return;
        }

        downloadsRepository.updateDownloadEndOfStream(originalDownloadInfo, state.currentBytes, state.contentLength);

        final boolean lengthMismatched = (state.contentLength != UNKNOWN_BYTE_SIZE) && (state.currentBytes != state.contentLength);
        if (lengthMismatched) {
            if (cannotResume(state)) {
                throw new StopRequestException(DownloadStatus.CANNOT_RESUME, "mismatched content length; unable to resume");
            } else {
                throw new StopRequestException(HTTP_DATA_ERROR, "closed socket before end of file");
            }
        }
    }

    private void updateStatusAndPause(State state) throws StopRequestException {
        downloadsRepository.pauseDownloadWithSize(originalDownloadInfo, state.currentBytes, state.totalBytes);
        throw new StopRequestException(DownloadStatus.PAUSED_BY_APP, "download paused by owner");
    }

    private boolean cannotResume(State state) {
        return (state.currentBytes > 0 && !originalDownloadInfo.isResumable() || DownloadDrmHelper.isDrmConvertNeeded(state.mimeType));
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

        updateDownloadInfoFieldsFrom(state);
        downloadsRepository.updateDatabaseFromHeaders(originalDownloadInfo, state.filename, state.headerETag, state.mimeType, state.totalBytes);
        // check connectivity again now that we know the total size
        checkConnectivity();
    }

    private void updateDownloadInfoFieldsFrom(State state) {
        originalDownloadInfo.setETag(state.headerETag);
        originalDownloadInfo.setMimeType(state.mimeType);
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
            state.contentLength = getHeaderFieldLong(conn, "Content-Length", UNKNOWN_BYTE_SIZE);
        } else {
            LLog.i("Ignoring Content-Length since Transfer-Encoding is also defined");
            state.contentLength = UNKNOWN_BYTE_SIZE;
        }

        state.totalBytes = state.contentLength;

        final boolean noSizeInfo = state.contentLength == UNKNOWN_BYTE_SIZE && (transferEncoding == null || !transferEncoding.equalsIgnoreCase("chunked"));
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
        LLog.i("have run thread before for id: " + originalDownloadInfo.getId() + ", and state.filename: " + state.filename);
        if (!Helpers.isFilenameValid(state.filename, storageManager.getDownloadDataDirectory())) {
            LLog.d("Yeah we know we are bad for downloading to internal storage");
//                throw new StopRequestException(Downloads.Impl.STATUS_FILE_ERROR, "found invalid internal destination filename");
        }
        // We're resuming a download that got interrupted
        File destinationFile = new File(state.filename);
        if (destinationFile.exists()) {
            LLog.i("resuming download for id: " + originalDownloadInfo.getId() + ", and state.filename: " + state.filename);
            long fileLength = destinationFile.length();
            if (fileLength == 0) {
                // The download hadn't actually started, we can restart from scratch
                LLog.d("setupDestinationFile() found fileLength=0, deleting " + state.filename);
                destinationFile.delete();
                state.filename = null;
                LLog.i("resuming download for id: " + originalDownloadInfo.getId() + ", BUT starting from scratch again: ");
            } else if (!originalDownloadInfo.isResumable()) {
                // This should've been caught upon failure
                LLog.d("setupDestinationFile() unable to resume download, deleting " + state.filename);
                destinationFile.delete();
                throw new StopRequestException(DownloadStatus.CANNOT_RESUME, "Trying to resume a download that can't be resumed");
            } else {
                // All right, we'll be able to resume this download
                LLog.i("resuming download for id: " + originalDownloadInfo.getId() + ", and starting with file of length: " + fileLength);
                state.currentBytes = (int) fileLength;
                if (originalDownloadInfo.getTotalBytes() != UNKNOWN_BYTE_SIZE) {
                    state.contentLength = originalDownloadInfo.getTotalBytes();
                }
                state.headerETag = originalDownloadInfo.getETag();
                state.continuingDownload = true;
                LLog.i("resuming download for id: " + originalDownloadInfo.getId() + ", state.currentBytes: " + state.currentBytes + ", and setting continuingDownload to true: ");
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
        downloadsRepository.updateDownload(originalDownloadInfo, state.filename,
                state.mimeType, state.retryAfter, state.requestUri, finalStatus, errorMsg, numFailed);

        updateBatchStatus(originalDownloadInfo.getBatchId(), originalDownloadInfo.getId());
    }

    private ContentResolver getContentResolver() {
        return context.getContentResolver();
    }

    private void updateBatchStatus(long batchId, long downloadId) {
        int batchStatus = batchRepository.calculateBatchStatus(batchId);

        batchRepository.updateBatchStatus(batchId, batchStatus);

        if (DownloadStatus.isCancelled(batchStatus)) {
            batchRepository.setBatchItemsCancelled(batchId);
        } else if (DownloadStatus.isFailure(batchStatus)) {
            batchRepository.setBatchItemsFailed(batchId, downloadId);
            batchInformationBroadcaster.notifyBatchFailedFor(batchId);
        } else if (DownloadStatus.isSuccess(batchStatus)) {
            batchInformationBroadcaster.notifyBatchCompletedFor(batchId);
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
            case QUEUED_DUE_CLIENT_RESTRICTIONS:
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
            LLog.e("Failed to set permissions. Unknown future behaviour.");
        }
    }
}
