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

import android.provider.BaseColumns;

/**
 * The Download Manager
 *
 * @pending
 */
final class Downloads {

    private Downloads() {
        // non-instantiable class
    }

    /**
     * Implementation details
     * <p/>
     * Exposes constants used to interact with the download manager's
     * content provider.
     * The constants URI ... STATUS are the names of columns in the downloads table.
     */
    static final class Impl implements BaseColumns {

        private Impl() {
        }

        /**
         * URI segment to access a publicly accessible downloaded file
         */
        public static final String PUBLICLY_ACCESSIBLE_DOWNLOADS_URI_SEGMENT = "public_downloads";

        /**
         * default value for {@link DownloadsColumns#COLUMN_LAST_UPDATESRC}.
         * This value is used when this column's value is not relevant.
         */
        public static final int LAST_UPDATESRC_NOT_RELEVANT = 0;

        /**
         * One of the values taken by {@link DownloadsColumns#COLUMN_LAST_UPDATESRC}.
         * This value is used when the update is NOT to be relayed to the DownloadService
         * (and thus spare DownloadService from scanning the database when this change occurs)
         */
        public static final int LAST_UPDATESRC_DONT_NOTIFY_DOWNLOADSVC = 1;

        /*
         * Lists the destinations that an application can specify for a download.
         */

        /**
         * This download is allowed to run.
         */
        public static final int CONTROL_RUN = 0;

        /**
         * This download must pause at the first opportunity.
         */
        public static final int CONTROL_PAUSED = 1;

        /*
         * Lists the states that the download manager can set on a download
         * to notify applications of the download progress.
         * The codes follow the HTTP families:<br>
         * 1xx: informational<br>
         * 2xx: success<br>
         * 3xx: redirects (not used by the download manager)<br>
         * 4xx: client errors<br>
         * 5xx: server errors
         */

        /**
         * Constants related to batches associated with each download.
         */
        public static class Batches implements BaseColumns {
            public static final String BATCHES_TABLE_NAME = "batches";

            /**
             * The name of the column where the initiating application can provided the
             * title of this batch. The title will be displayed ito the user in the
             * list of batches.
             * <P>Type: TEXT</P>
             * <P>Owner can Init/Read/Write</P>
             */
            public static final String COLUMN_TITLE = "batch_title";

            /**
             * The name of the column where the initiating application can provide the
             * description of this batch. The description will be displayed to the
             * user in the list of batches.
             * <P>Type: TEXT</P>
             * <P>Owner can Init/Read/Write</P>
             */
            public static final String COLUMN_DESCRIPTION = "batch_description";

            /**
             * A URL that will be used to show a big picture style notification
             */
            public static final String COLUMN_BIG_PICTURE = "batch_notificationBigPictureResourceId";

            /**
             * The status of the batch.
             * <P>Type: INTEGER</P>
             * <P>Owner can Read</P>
             */
            public static final String COLUMN_STATUS = "batch_status";

            /**
             * The name of the column containing the flags that controls whether the
             * batch is displayed by the UI. See the {@link NotificationVisibility} constants for
             * a list of legal values.
             * <P>Type: INTEGER</P>
             * <P>Owner can Init/Read/Write</P>
             */
            public static final String COLUMN_VISIBILITY = "visibility";

            /**
             * Set to true if this batch is deleted. Its downloads will also be deleted.
             * <P>Type: BOOLEAN</P>
             * <P>Owner can Read</P>
             */
            public static final String COLUMN_DELETED = "deleted";

            /**
             * The total size of the batch in bytes.
             * <P>Type: INTEGER</P>
             */
            public static final String COLUMN_TOTAL_BYTES = "batch_total_bytes";

            /**
             * The current size of the batch in bytes (on device).
             * <P>Type: INTEGER</P>
             */
            public static final String COLUMN_CURRENT_BYTES = "batch_current_bytes";
        }
    }

    /**
     * Query where clause for general querying.
     */
    private static final String QUERY_WHERE_CLAUSE = DownloadsColumns.COLUMN_NOTIFICATION_CLASS + "=?";

}
