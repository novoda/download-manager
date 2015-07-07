/*
 * Copyright (C) 2013 The Android Open Source Project
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
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.SystemClock;

import com.novoda.notils.logger.simple.Log;

import java.util.HashMap;
import java.util.Map;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

/**
 * Manages asynchronous scanning of completed downloads.
 */
class DownloadScanner implements MediaScannerConnectionClient {
    private static final long SCAN_TIMEOUT = MINUTE_IN_MILLIS;

    private final ContentResolver resolver;
    private final MediaScannerConnection mediaScannerConnection;

    private static class ScanRequest {
        public final long id;
        public final String path;
        public final String mimeType;
        public final long requestRealtime;

        public ScanRequest(long id, String path, String mimeType) {
            this.id = id;
            this.path = path;
            this.mimeType = mimeType;
            this.requestRealtime = SystemClock.elapsedRealtime();
        }

        public void exec(MediaScannerConnection conn) {
            conn.scanFile(path, mimeType);
        }
    }

    //    @GuardedBy("mediaScannerConnection")
    private Map<String, ScanRequest> pendingRequests = new HashMap<>();

    public static DownloadScanner newInstance(Context context){
        return new DownloadScanner(context.getContentResolver(), context);
    }

    public DownloadScanner(ContentResolver resolver, Context context) {
        this.resolver = resolver;
        mediaScannerConnection = new MediaScannerConnection(context, this);
    }

    /**
     * Check if requested scans are still pending. Scans may timeout after an
     * internal duration.
     */
    public boolean hasPendingScans() {
        synchronized (mediaScannerConnection) {
            if (pendingRequests.isEmpty()) {
                return false;
            } else {
                // Check if pending scans have timed out
                final long nowRealtime = SystemClock.elapsedRealtime();
                for (ScanRequest req : pendingRequests.values()) {
                    if (nowRealtime < req.requestRealtime + SCAN_TIMEOUT) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /**
     * Request that given {@link DownloadInfo} be scanned at some point in
     * future. Enqueues the request to be scanned asynchronously.
     *
     * @see #hasPendingScans()
     */
    public void requestScan(DownloadInfo info) {
        Log.v("requestScan() for " + info.getFileName());
        synchronized (mediaScannerConnection) {
            final ScanRequest req = new ScanRequest(info.getId(), info.getFileName(), info.getMimeType());
            pendingRequests.put(req.path, req);

            if (mediaScannerConnection.isConnected()) {
                req.exec(mediaScannerConnection);
            } else {
                mediaScannerConnection.connect();
            }
        }
    }

    public void shutdown() {
        mediaScannerConnection.disconnect();
    }

    @Override
    public void onMediaScannerConnected() {
        synchronized (mediaScannerConnection) {
            for (ScanRequest req : pendingRequests.values()) {
                req.exec(mediaScannerConnection);
            }
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        final ScanRequest req;
        synchronized (mediaScannerConnection) {
            req = pendingRequests.remove(path);
        }
        if (req == null) {
            Log.w("Missing request for path " + path);
            return;
        }

        // Update scanned column, which will kick off a database update pass,
        // eventually deciding if overall service is ready for teardown.
        final ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_MEDIA_SCANNED, 1);
        if (uri != null) {
            values.put(Downloads.Impl.COLUMN_MEDIAPROVIDER_URI, uri.toString());
        }

        final Uri downloadUri = ContentUris.withAppendedId(
                Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, req.id);
        final int rows = resolver.update(downloadUri, values, null, null);
        if (rows == 0) {
            // Local row disappeared during scan; download was probably deleted
            // so clean up now-orphaned media entry.
            resolver.delete(uri, null, null);
        }
    }
}
