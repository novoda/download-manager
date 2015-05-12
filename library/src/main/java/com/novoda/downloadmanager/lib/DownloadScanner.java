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

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

/**
 * Manages asynchronous scanning of completed downloads.
 */
public class DownloadScanner implements MediaScannerConnectionClient {
    private static final long SCAN_TIMEOUT = MINUTE_IN_MILLIS;

    private final Context mContext;
    private final MediaScannerConnection mConnection;
    private final String mAuthority;

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

    //    @GuardedBy("mConnection")
    private HashMap<String, ScanRequest> mPending = new HashMap<String, ScanRequest>();

    public DownloadScanner(Context context) {
        mContext = context;
        mConnection = new MediaScannerConnection(context, this);
        mAuthority = DownloadProvider.determineAuthority(context);
    }

    /**
     * Check if requested scans are still pending. Scans may timeout after an
     * internal duration.
     */
    public boolean hasPendingScans() {
        synchronized (mConnection) {
            if (mPending.isEmpty()) {
                return false;
            } else {
                // Check if pending scans have timed out
                final long nowRealtime = SystemClock.elapsedRealtime();
                for (ScanRequest req : mPending.values()) {
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
        Log.v("requestScan() for " + info.mFileName);
        synchronized (mConnection) {
            final ScanRequest req = new ScanRequest(info.mId, info.mFileName, info.mMimeType);
            mPending.put(req.path, req);

            if (mConnection.isConnected()) {
                req.exec(mConnection);
            } else {
                mConnection.connect();
            }
        }
    }

    public void shutdown() {
        mConnection.disconnect();
    }

    @Override
    public void onMediaScannerConnected() {
        synchronized (mConnection) {
            for (ScanRequest req : mPending.values()) {
                req.exec(mConnection);
            }
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        final ScanRequest req;
        synchronized (mConnection) {
            req = mPending.remove(path);
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

        final ContentResolver resolver = mContext.getContentResolver();
        final Uri downloadUri = ContentUris.withAppendedId(
                Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI(mAuthority), req.id);
        final int rows = resolver.update(downloadUri, values, null, null);
        if (rows == 0) {
            // Local row disappeared during scan; download was probably deleted
            // so clean up now-orphaned media entry.
            resolver.delete(uri, null, null);
        }
    }
}
