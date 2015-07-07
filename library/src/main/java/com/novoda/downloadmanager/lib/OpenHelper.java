/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;

import static android.app.DownloadManager.*;
import static com.novoda.downloadmanager.lib.Downloads.Impl.RequestHeaders;

class OpenHelper {

    private final Downloads downloads;

    public OpenHelper(Downloads downloads) {
        this.downloads = downloads;
    }
    /**
     * Build an {@link Intent} to view the download at current {@link Cursor}
     * position, handling subtleties around installing packages.
     */
    public Intent buildViewIntent(Context context, long id) {
        final DownloadManager downManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downManager.setAccessAllDownloads(true);

        final Cursor cursor = downManager.query(new Query().setFilterById(id));
        try {
            if (!cursor.moveToFirst()) {
                throw new IllegalArgumentException("Missing download " + id);
            }

            final Uri localUri = getCursorUri(cursor, COLUMN_LOCAL_URI);
            final File file = getCursorFile(cursor, COLUMN_LOCAL_FILENAME);
            String mimeType = getCursorString(cursor, COLUMN_MEDIA_TYPE);
            mimeType = DownloadDrmHelper.getOriginalMimeType(context, file, mimeType);

            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if ("application/vnd.android.package-archive".equals(mimeType)) {
                // PackageInstaller doesn't like content URIs, so open file
                intent.setDataAndType(localUri, mimeType);

                // Also splice in details about where it came from
                final Uri remoteUri = getCursorUri(cursor, COLUMN_URI);
                intent.putExtra("android.intent.extra.ORIGINATING_URI", remoteUri);
                intent.putExtra("android.intent.extra.REFERRER", getRefererUri(context, id));
                intent.putExtra("android.intent.extra.ORIGINATING_UID", getOriginatingUid(context, id));
            } else if ("file".equals(localUri.getScheme())) {
                intent.setDataAndType(
                        ContentUris.withAppendedId(downloads.getAllDownloadsContentUri(), id), mimeType);
            } else {
                intent.setDataAndType(localUri, mimeType);
            }

            return intent;
        } finally {
            cursor.close();
        }
    }

    private Uri getRefererUri(Context context, long id) {
        final Uri headersUri = Uri.withAppendedPath(
                ContentUris.withAppendedId(downloads.getAllDownloadsContentUri(), id),
                RequestHeaders.URI_SEGMENT);
        final Cursor headers = context.getContentResolver()
                .query(headersUri, null, null, null, null);
        try {
            while (headers.moveToNext()) {
                final String header = getCursorString(headers, RequestHeaders.COLUMN_HEADER);
                if ("Referer".equalsIgnoreCase(header)) {
                    return getCursorUri(headers, RequestHeaders.COLUMN_VALUE);
                }
            }
        } finally {
            headers.close();
        }
        return null;
    }

    private int getOriginatingUid(Context context, long id) {
        final Uri uri = ContentUris.withAppendedId(downloads.getAllDownloadsContentUri(), id);
        final Cursor cursor = context.getContentResolver().query(uri, new String[]{Constants.UID},
                null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(cursor.getColumnIndexOrThrow(Constants.UID));
                }
            } finally {
                cursor.close();
            }
        }
        return -1;
    }

    private static String getCursorString(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }

    private static Uri getCursorUri(Cursor cursor, String column) {
        return Uri.parse(getCursorString(cursor, column));
    }

    private static long getCursorLong(Cursor cursor, String column) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(column));
    }

    private static File getCursorFile(Cursor cursor, String column) {
        return new File(cursor.getString(cursor.getColumnIndexOrThrow(column)));
    }
}
