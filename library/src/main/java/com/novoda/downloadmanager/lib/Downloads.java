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

    }

    /**
     * Query where clause for general querying.
     */
    private static final String QUERY_WHERE_CLAUSE = DownloadsColumns.COLUMN_NOTIFICATION_CLASS + "=?";

}
