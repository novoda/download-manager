/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import com.novoda.downloadmanager.BuildConfig;
import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Allows application to interact with the download manager.
 */
public final class DownloadProvider extends ContentProvider {

    /**
     * Added so we can use our own ContentProvider
     */
    public static final String AUTHORITY = Reflector.reflectAuthority();

    /**
     * Database filename
     */
    private static final String DB_NAME = "downloads.db";

    /**
     * Current database version
     */
    private static final int DB_VERSION = 109;

    /**
     * Name of table in the database
     */
    private static final String DB_TABLE = "Downloads";

    /**
     * MIME type for the entire download list
     */
    private static final String DOWNLOAD_LIST_TYPE = "vnd.android.cursor.dir/download";
    /**
     * MIME type for an individual download
     */
    private static final String DOWNLOAD_TYPE = "vnd.android.cursor.item/download";

    /**
     * URI matcher used to recognize URIs sent by applications
     */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    /**
     * URI matcher constant for the URI of all downloads belonging to the calling UID
     */
    private static final int MY_DOWNLOADS = 1;
    /**
     * URI matcher constant for the URI of an individual download belonging to the calling UID
     */
    private static final int MY_DOWNLOADS_ID = 2;
    /**
     * URI matcher constant for the URI of all downloads in the system
     */
    private static final int ALL_DOWNLOADS = 3;
    /**
     * URI matcher constant for the URI of an individual download
     */
    private static final int ALL_DOWNLOADS_ID = 4;
    /**
     * URI matcher constant for the URI of a download's request headers
     */
    private static final int REQUEST_HEADERS_URI = 5;
    /**
     * URI matcher constant for the public URI returned by
     * {@link DownloadManager#getUriForDownloadedFile(long)} if the given downloaded file
     * is publicly accessible.
     */
    private static final int PUBLIC_DOWNLOAD_ID = 6;

    static {
        sURIMatcher.addURI(AUTHORITY, "my_downloads", MY_DOWNLOADS);
        sURIMatcher.addURI(AUTHORITY, "my_downloads/#", MY_DOWNLOADS_ID);
        sURIMatcher.addURI(AUTHORITY, "all_downloads", ALL_DOWNLOADS);
        sURIMatcher.addURI(AUTHORITY, "all_downloads/#", ALL_DOWNLOADS_ID);
        sURIMatcher.addURI(AUTHORITY, "my_downloads/#/" + Downloads.Impl.RequestHeaders.URI_SEGMENT, REQUEST_HEADERS_URI);
        sURIMatcher.addURI(AUTHORITY, "all_downloads/#/" + Downloads.Impl.RequestHeaders.URI_SEGMENT, REQUEST_HEADERS_URI);
        // temporary, for backwards compatibility
        sURIMatcher.addURI(AUTHORITY, "download", MY_DOWNLOADS);
        sURIMatcher.addURI(AUTHORITY, "download/#", MY_DOWNLOADS_ID);
        sURIMatcher.addURI(AUTHORITY, "download/#/" + Downloads.Impl.RequestHeaders.URI_SEGMENT, REQUEST_HEADERS_URI);
        sURIMatcher.addURI(AUTHORITY, Downloads.Impl.PUBLICLY_ACCESSIBLE_DOWNLOADS_URI_SEGMENT + "/#", PUBLIC_DOWNLOAD_ID);
    }

    /**
     * Different base URIs that could be used to access an individual download
     */
    private static final Uri[] BASE_URIS = new Uri[]{
            Downloads.Impl.CONTENT_URI,
            Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI,
    };

    private static final String[] sAppReadableColumnsArray = new String[]{
            Downloads.Impl._ID,
            Downloads.Impl.COLUMN_APP_DATA,
            Downloads.Impl._DATA,
            Downloads.Impl.COLUMN_MIME_TYPE,
            Downloads.Impl.COLUMN_VISIBILITY,
            Downloads.Impl.COLUMN_DESTINATION,
            Downloads.Impl.COLUMN_CONTROL,
            Downloads.Impl.COLUMN_STATUS,
            Downloads.Impl.COLUMN_LAST_MODIFICATION,
            Downloads.Impl.COLUMN_NOTIFICATION_CLASS,
            Downloads.Impl.COLUMN_TOTAL_BYTES,
            Downloads.Impl.COLUMN_CURRENT_BYTES,
            Downloads.Impl.COLUMN_TITLE,
            Downloads.Impl.COLUMN_DESCRIPTION,
            Downloads.Impl.COLUMN_URI,
            Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI,
            Downloads.Impl.COLUMN_FILE_NAME_HINT,
            Downloads.Impl.COLUMN_MEDIAPROVIDER_URI,
            Downloads.Impl.COLUMN_DELETED,
            Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS,
            Downloads.Impl.COLUMN_BIG_PICTURE,
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE,
    };

    private static final HashSet<String> sAppReadableColumnsSet;
    private static final HashMap<String, String> sColumnsMap;

    static {
        sAppReadableColumnsSet = new HashSet<String>();
        Collections.addAll(sAppReadableColumnsSet, sAppReadableColumnsArray);

        sColumnsMap = new HashMap<String, String>();
        sColumnsMap.put(OpenableColumns.DISPLAY_NAME, Downloads.Impl.COLUMN_TITLE + " AS " + OpenableColumns.DISPLAY_NAME);
        sColumnsMap.put(OpenableColumns.SIZE, Downloads.Impl.COLUMN_TOTAL_BYTES + " AS " + OpenableColumns.SIZE);
    }

    private static final List<String> downloadManagerColumnsList = Arrays.asList(DownloadManager.UNDERLYING_COLUMNS);

    /**
     * The database that lies underneath this content provider
     */
    private SQLiteOpenHelper mOpenHelper = null;

    /**
     * List of uids that can access the downloads
     */
    private int mSystemUid = -1;
    private int mDefContainerUid = -1;
    private File mDownloadsDataDir;

    //    @VisibleForTesting
    SystemFacade mSystemFacade;

    /**
     * This class encapsulates a SQL where clause and its parameters.  It makes it possible for
     * shared methods (like {@link DownloadProvider#getWhereClause(Uri, String, String[], int)})
     * to return both pieces of information, and provides some utility logic to ease piece-by-piece
     * construction of selections.
     */
    private static class SqlSelection {
        public StringBuilder mWhereClause = new StringBuilder();
        public List<String> mParameters = new ArrayList<String>();

        public <T> void appendClause(String newClause, final T... parameters) {
            if (newClause == null || newClause.isEmpty()) {
                return;
            }
            if (mWhereClause.length() != 0) {
                mWhereClause.append(" AND ");
            }
            mWhereClause.append("(");
            mWhereClause.append(newClause);
            mWhereClause.append(")");
            if (parameters != null) {
                for (Object parameter : parameters) {
                    mParameters.add(parameter.toString());
                }
            }
        }

        public String getSelection() {
            return mWhereClause.toString();
        }

        public String[] getParameters() {
            String[] array = new String[mParameters.size()];
            return mParameters.toArray(array);
        }
    }

    /**
     * Creates and updated database on demand when opening it.
     * Helper class to create database the first time the provider is
     * initialized and upgrade it when a new version of the provider needs
     * an updated version of the database.
     */
    private final class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * Creates database the first time we try to open it.
         */
        @Override
        public void onCreate(final SQLiteDatabase db) {
            Log.v("populating new database");
            onUpgrade(db, 0, DB_VERSION);
        }

        /**
         * Updates the database format when a content provider is used
         * with a database that was created with a different format.
         * <p/>
         * Note: to support downgrades, creating a table should always drop it first if it already
         * exists.
         */
        @Override
        public void onUpgrade(final SQLiteDatabase db, int oldV, final int newV) {
            if (oldV == 31) {
                // 31 and 100 are identical, just in different codelines. Upgrading from 31 is the
                // same as upgrading from 100.
                oldV = 100;
            } else if (oldV < 100) {
                // no logic to upgrade from these older version, just recreate the DB
                Log.i("Upgrading downloads database from version " + oldV + " to version " + newV + ", which will destroy all old data");
                oldV = 99;
            } else if (oldV > newV) {
                // user must have downgraded software; we have no way to know how to downgrade the
                // DB, so just recreate it
                Log.i("Downgrading downloads database from version " + oldV + " (current version is " + newV + "), destroying all old data");
                oldV = 99;
            }

            for (int version = oldV + 1; version <= newV; version++) {
                upgradeTo(db, version);
            }
        }

        /**
         * Upgrade database from (version - 1) to version.
         */
        private void upgradeTo(SQLiteDatabase db, int version) {
            switch (version) {
                case 100:
                    createDownloadsTable(db);
                    break;

                case 101:
                    createHeadersTable(db);
                    break;

                case 102:
                    addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_ALLOW_ROAMING, "INTEGER NOT NULL DEFAULT 0");
                    addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES, "INTEGER NOT NULL DEFAULT 0");
                    break;

                case 103:
                    addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, "INTEGER NOT NULL DEFAULT 1");
                    makeCacheDownloadsInvisible(db);
                    break;

                case 104:
                    addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT, "INTEGER NOT NULL DEFAULT 0");
                    break;

                case 105:
                    fillNullValues(db);
                    break;

                case 106:
                    addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_MEDIAPROVIDER_URI, "TEXT");
                    addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_DELETED, "BOOLEAN NOT NULL DEFAULT 0");
                    break;

                case 107:
                    addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_ERROR_MSG, "TEXT");
                    break;

                case 108:
                    addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_ALLOW_METERED, "INTEGER NOT NULL DEFAULT 1");
                    break;

                case 109:
                    addColumn(db, DB_TABLE, Downloads.Impl.COLUMN_BIG_PICTURE, "TEXT");
                    break;

                default:
                    throw new IllegalStateException("Don't know how to upgrade to " + version);
            }
        }

        /**
         * insert() now ensures these four columns are never null for new downloads, so this method
         * makes that true for existing columns, so that code can rely on this assumption.
         */
        private void fillNullValues(SQLiteDatabase db) {
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, 0);
            fillNullValuesForColumn(db, values);
            values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, -1);
            fillNullValuesForColumn(db, values);
            values.put(Downloads.Impl.COLUMN_TITLE, "");
            fillNullValuesForColumn(db, values);
            values.put(Downloads.Impl.COLUMN_DESCRIPTION, "");
            fillNullValuesForColumn(db, values);
        }

        private void fillNullValuesForColumn(SQLiteDatabase db, ContentValues values) {
            String column = values.valueSet().iterator().next().getKey();
            db.update(DB_TABLE, values, column + " is null", null);
            values.clear();
        }

        /**
         * Set all existing downloads to the cache partition to be invisible in the downloads UI.
         */
        private void makeCacheDownloadsInvisible(SQLiteDatabase db) {
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, false);
            String cacheSelection = Downloads.Impl.COLUMN_DESTINATION + " != " + Downloads.Impl.DESTINATION_EXTERNAL;
            db.update(DB_TABLE, values, cacheSelection, null);
        }

        /**
         * Add a column to a table using ALTER TABLE.
         *
         * @param dbTable          name of the table
         * @param columnName       name of the column to add
         * @param columnDefinition SQL for the column definition
         */
        private void addColumn(SQLiteDatabase db, String dbTable, String columnName, String columnDefinition) {
            db.execSQL("ALTER TABLE " + dbTable + " ADD COLUMN " + columnName + " " + columnDefinition);
        }

        /**
         * Creates the table that'll hold the download information.
         */
        private void createDownloadsTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
                db.execSQL("CREATE TABLE " + DB_TABLE + "(" +
                        Downloads.Impl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Downloads.Impl.COLUMN_URI + " TEXT, " +
                        Constants.RETRY_AFTER_X_REDIRECT_COUNT + " INTEGER, " +
                        Downloads.Impl.COLUMN_APP_DATA + " TEXT, " +
                        Downloads.Impl.COLUMN_NO_INTEGRITY + " BOOLEAN, " +
                        Downloads.Impl.COLUMN_FILE_NAME_HINT + " TEXT, " +
                        Constants.OTA_UPDATE + " BOOLEAN, " +
                        Downloads.Impl._DATA + " TEXT, " +
                        Downloads.Impl.COLUMN_MIME_TYPE + " TEXT, " +
                        Downloads.Impl.COLUMN_DESTINATION + " INTEGER, " +
                        Constants.NO_SYSTEM_FILES + " BOOLEAN, " +
                        Downloads.Impl.COLUMN_VISIBILITY + " INTEGER, " +
                        Downloads.Impl.COLUMN_CONTROL + " INTEGER, " +
                        Downloads.Impl.COLUMN_STATUS + " INTEGER, " +
                        Downloads.Impl.COLUMN_FAILED_CONNECTIONS + " INTEGER, " +
                        Downloads.Impl.COLUMN_LAST_MODIFICATION + " BIGINT, " +
                        Downloads.Impl.COLUMN_NOTIFICATION_CLASS + " TEXT, " +
                        Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS + " TEXT, " +
                        Downloads.Impl.COLUMN_COOKIE_DATA + " TEXT, " +
                        Downloads.Impl.COLUMN_USER_AGENT + " TEXT, " +
                        Downloads.Impl.COLUMN_REFERER + " TEXT, " +
                        Downloads.Impl.COLUMN_TOTAL_BYTES + " INTEGER, " +
                        Downloads.Impl.COLUMN_CURRENT_BYTES + " INTEGER, " +
                        Constants.ETAG + " TEXT, " +
                        Constants.UID + " INTEGER, " +
                        Downloads.Impl.COLUMN_OTHER_UID + " INTEGER, " +
                        Downloads.Impl.COLUMN_TITLE + " TEXT, " +
                        Downloads.Impl.COLUMN_DESCRIPTION + " TEXT, " +
                        Constants.MEDIA_SCANNED + " BOOLEAN);");
            } catch (SQLException ex) {
                Log.e("couldn't create table in downloads database");
                throw ex;
            }
        }

        private void createHeadersTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE);
            db.execSQL("CREATE TABLE " + Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID + " INTEGER NOT NULL," +
                    Downloads.Impl.RequestHeaders.COLUMN_HEADER + " TEXT NOT NULL," +
                    Downloads.Impl.RequestHeaders.COLUMN_VALUE + " TEXT NOT NULL" +
                    ");");
        }
    }

    /**
     * Initializes the content provider when it is created.
     */
    @Override
    public boolean onCreate() {
        if (mSystemFacade == null) {
            mSystemFacade = new RealSystemFacade(getContext());
        }

        mOpenHelper = new DatabaseHelper(getContext());
        // Initialize the system uid
        mSystemUid = Process.SYSTEM_UID;
        // Initialize the default container uid. Package name hardcoded
        // for now.
        ApplicationInfo appInfo = null;
        try {
            appInfo = getContext().getPackageManager().
                    getApplicationInfo("com.android.defcontainer", 0);
        } catch (NameNotFoundException e) {
            Log.wtf("Could not get ApplicationInfo for com.android.defconatiner", e);
        }
        if (appInfo != null) {
            mDefContainerUid = appInfo.uid;
        }
        // start the DownloadService class. don't wait for the 1st download to be issued.
        // saves us by getting some initialization code in DownloadService out of the way.
        Context context = getContext();
        context.startService(new Intent(context, DownloadService.class));
//        mDownloadsDataDir = StorageManager.getDownloadDataDirectory(getContext());
        mDownloadsDataDir = context.getCacheDir();
//        try {
//            android.os.SELinux.restorecon(mDownloadsDataDir.getCanonicalPath());
//        } catch (IOException e) {
//            Log.wtf("Could not get canonical path for download directory", e);
//        }
        return true;
    }

    /**
     * Returns the content-provider-style MIME types of the various
     * types accessible through this content provider.
     */
    @Override
    public String getType(final Uri uri) {
        int match = sURIMatcher.match(uri);
        switch (match) {
            case MY_DOWNLOADS:
            case ALL_DOWNLOADS: {
                return DOWNLOAD_LIST_TYPE;
            }
            case MY_DOWNLOADS_ID:
            case ALL_DOWNLOADS_ID:
            case PUBLIC_DOWNLOAD_ID: {
                // return the mimetype of this id from the database
                final String id = getDownloadIdFromUri(uri);
                final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
                final String mimeType = DatabaseUtils.stringForQuery(db,
                        "SELECT " + Downloads.Impl.COLUMN_MIME_TYPE + " FROM " + DB_TABLE +
                                " WHERE " + Downloads.Impl._ID + " = ?",
                        new String[]{id});
                if (TextUtils.isEmpty(mimeType)) {
                    return DOWNLOAD_TYPE;
                } else {
                    return mimeType;
                }
            }
            default: {
                Log.v("calling getType on an unknown URI: " + uri);
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
    }

    /**
     * Inserts a row in the database
     */
    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        checkInsertPermissions(values);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // note we disallow inserting into ALL_DOWNLOADS
        int match = sURIMatcher.match(uri);
        if (match != MY_DOWNLOADS) {
            Log.d("calling insert on an unknown/invalid URI: " + uri);
            throw new IllegalArgumentException("Unknown/Invalid URI " + uri);
        }

        // copy some of the input values as it
        ContentValues filteredValues = new ContentValues();
        copyString(Downloads.Impl.COLUMN_URI, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_APP_DATA, values, filteredValues);
        copyBoolean(Downloads.Impl.COLUMN_NO_INTEGRITY, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_FILE_NAME_HINT, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_MIME_TYPE, values, filteredValues);

        // validate the destination column
        Integer dest = values.getAsInteger(Downloads.Impl.COLUMN_DESTINATION);
        if (dest != null) {
            if (getContext().checkCallingPermission(Downloads.Impl.PERMISSION_ACCESS_ADVANCED)
                    != PackageManager.PERMISSION_GRANTED
                    && (dest == Downloads.Impl.DESTINATION_CACHE_PARTITION
                    || dest == Downloads.Impl.DESTINATION_CACHE_PARTITION_NOROAMING
                    || dest == Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION)) {
                throw new SecurityException("setting destination to : " + dest +
                        " not allowed, unless PERMISSION_ACCESS_ADVANCED is granted");
            }
            // for public API behavior, if an app has CACHE_NON_PURGEABLE permission, automatically
            // switch to non-purgeable download
            boolean hasNonPurgeablePermission =
                    getContext().checkCallingPermission(Downloads.Impl.PERMISSION_CACHE_NON_PURGEABLE) == PackageManager.PERMISSION_GRANTED;
            if (dest == Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE && hasNonPurgeablePermission) {
                dest = Downloads.Impl.DESTINATION_CACHE_PARTITION;
            }
            if (dest == Downloads.Impl.DESTINATION_FILE_URI) {
                getContext().enforcePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Binder.getCallingPid(), Binder.getCallingUid(),
                        "need WRITE_EXTERNAL_STORAGE permission to use DESTINATION_FILE_URI");
                checkFileUriDestination(values);
            } else if (dest == Downloads.Impl.DESTINATION_SYSTEMCACHE_PARTITION) {
                getContext().enforcePermission("android.permission.ACCESS_CACHE_FILESYSTEM", Binder.getCallingPid(), Binder.getCallingUid(),
                        "need ACCESS_CACHE_FILESYSTEM permission to use system cache");
            }
            filteredValues.put(Downloads.Impl.COLUMN_DESTINATION, dest);
        }

        // validate the visibility column
        Integer vis = values.getAsInteger(Downloads.Impl.COLUMN_VISIBILITY);
        if (vis == null) {
            if (dest == Downloads.Impl.DESTINATION_EXTERNAL) {
                filteredValues.put(Downloads.Impl.COLUMN_VISIBILITY, Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            } else {
                filteredValues.put(Downloads.Impl.COLUMN_VISIBILITY, Downloads.Impl.VISIBILITY_HIDDEN);
            }
        } else {
            filteredValues.put(Downloads.Impl.COLUMN_VISIBILITY, vis);
        }
        // copy the control column as is
        copyInteger(Downloads.Impl.COLUMN_CONTROL, values, filteredValues);

        /*
         * requests coming from
         * DownloadManager.addCompletedDownload(String, String, String,
         * boolean, String, String, long) need special treatment
         */
        if (values.getAsInteger(Downloads.Impl.COLUMN_DESTINATION) == Downloads.Impl.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD) {
            // these requests always are marked as 'completed'
            filteredValues.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_SUCCESS);
            filteredValues.put(Downloads.Impl.COLUMN_TOTAL_BYTES, values.getAsLong(Downloads.Impl.COLUMN_TOTAL_BYTES));
            filteredValues.put(Downloads.Impl.COLUMN_CURRENT_BYTES, 0);
            copyInteger(Downloads.Impl.COLUMN_MEDIA_SCANNED, values, filteredValues);
            copyString(Downloads.Impl._DATA, values, filteredValues);
        } else {
            filteredValues.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PENDING);
            filteredValues.put(Downloads.Impl.COLUMN_TOTAL_BYTES, -1);
            filteredValues.put(Downloads.Impl.COLUMN_CURRENT_BYTES, 0);
        }

        // set lastupdate to current time
        long lastMod = mSystemFacade.currentTimeMillis();
        filteredValues.put(Downloads.Impl.COLUMN_LAST_MODIFICATION, lastMod);

        // use packagename of the caller to set the notification columns
        String clazz = values.getAsString(Downloads.Impl.COLUMN_NOTIFICATION_CLASS);
        if (clazz != null) {
            int uid = Binder.getCallingUid();
            try {
                if ((uid == 0) || mSystemFacade.userOwnsPackage(uid, getContext().getPackageName())) {
                    if (clazz != null) {
                        filteredValues.put(Downloads.Impl.COLUMN_NOTIFICATION_CLASS, clazz);
                    }
                }
            } catch (PackageManager.NameNotFoundException ex) {
                /* ignored for now */
            }
        }

        // copy some more columns as is
        copyString(Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_BIG_PICTURE, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_COOKIE_DATA, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_USER_AGENT, values, filteredValues);
        copyString(Downloads.Impl.COLUMN_REFERER, values, filteredValues);

        // UID, PID columns
        if (getContext().checkCallingPermission(Downloads.Impl.PERMISSION_ACCESS_ADVANCED) == PackageManager.PERMISSION_GRANTED) {
            copyInteger(Downloads.Impl.COLUMN_OTHER_UID, values, filteredValues);
        }
        filteredValues.put(Constants.UID, Binder.getCallingUid());
        if (Binder.getCallingUid() == 0) {
            copyInteger(Constants.UID, values, filteredValues);
        }

        // copy some more columns as is
        copyStringWithDefault(Downloads.Impl.COLUMN_TITLE, values, filteredValues, "");
        copyStringWithDefault(Downloads.Impl.COLUMN_DESCRIPTION, values, filteredValues, "");

        // is_visible_in_downloads_ui column
        if (values.containsKey(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI)) {
            copyBoolean(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, values, filteredValues);
        } else {
            // by default, make external downloads visible in the UI
            boolean isExternal = (dest == null || dest == Downloads.Impl.DESTINATION_EXTERNAL);
            filteredValues.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, isExternal);
        }

        // public api requests and networktypes/roaming columns
        copyInteger(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES, values, filteredValues);
        copyBoolean(Downloads.Impl.COLUMN_ALLOW_ROAMING, values, filteredValues);
        copyBoolean(Downloads.Impl.COLUMN_ALLOW_METERED, values, filteredValues);

        Log.v("initiating download with UID " + filteredValues.getAsInteger(Constants.UID));
        if (filteredValues.containsKey(Downloads.Impl.COLUMN_OTHER_UID)) {
            Log.v("other UID " + filteredValues.getAsInteger(Downloads.Impl.COLUMN_OTHER_UID));
        }

        long rowID = db.insert(DB_TABLE, null, filteredValues);
        if (rowID == -1) {
            Log.d("couldn't insert into downloads database");
            return null;
        }

        insertRequestHeaders(db, rowID, values);
        /*
         * requests coming from
         * DownloadManager.addCompletedDownload(String, String, String,
         * boolean, String, String, long) need special treatment
         */
        Context context = getContext();
        if (values.getAsInteger(Downloads.Impl.COLUMN_DESTINATION) == Downloads.Impl.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD) {
            // When notification is requested, kick off service to process all relevant downloads.
            if (Downloads.Impl.isNotificationToBeDisplayed(vis)) {
                context.startService(new Intent(context, DownloadService.class));
            }
        } else {
            context.startService(new Intent(context, DownloadService.class));
        }
        notifyContentChanged(uri, match);
        return ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, rowID);
    }

    /**
     * Check that the file URI provided for DESTINATION_FILE_URI is valid.
     */
    private void checkFileUriDestination(ContentValues values) {
        String fileUri = values.getAsString(Downloads.Impl.COLUMN_FILE_NAME_HINT);
        if (fileUri == null) {
            throw new IllegalArgumentException(
                    "DESTINATION_FILE_URI must include a file URI under COLUMN_FILE_NAME_HINT");
        }
        Uri uri = Uri.parse(fileUri);
        String scheme = uri.getScheme();
        if (scheme == null || !scheme.equals("file")) {
            throw new IllegalArgumentException("Not a file URI: " + uri);
        }
        final String path = uri.getPath();
        if (path == null) {
            throw new IllegalArgumentException("Invalid file URI: " + uri);
        }
//        try {
//            final String canonicalPath = new File(path).getCanonicalPath();
//            final String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            if (!canonicalPath.startsWith(externalPath)) {
//                throw new SecurityException("Destination must be on external storage: " + uri);
//            }
//        } catch (IOException e) {
//            throw new SecurityException("Problem resolving path: " + uri);
//        }
    }

    /**
     * Apps with the ACCESS_DOWNLOAD_MANAGER permission can access this provider freely, subject to
     * constraints in the rest of the code. Apps without that may still access this provider through
     * the public API, but additional restrictions are imposed. We check those restrictions here.
     *
     * @param values ContentValues provided to insert()
     * @throws SecurityException if the caller has insufficient permissions
     */
    private void checkInsertPermissions(ContentValues values) {
        if (getContext().checkCallingOrSelfPermission(Downloads.Impl.PERMISSION_ACCESS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        getContext().enforceCallingOrSelfPermission(android.Manifest.permission.INTERNET, "INTERNET permission is required to use the download manager");

        // ensure the request fits within the bounds of a public API request
        // first copy so we can remove values
        values = new ContentValues(values);

        // validate the destination column
        if (values.getAsInteger(Downloads.Impl.COLUMN_DESTINATION) == Downloads.Impl.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD) {
            /* this row is inserted by
             * DownloadManager.addCompletedDownload(String, String, String, boolean, String, String, long)
             */
            values.remove(Downloads.Impl.COLUMN_TOTAL_BYTES);
            values.remove(Downloads.Impl._DATA);
            values.remove(Downloads.Impl.COLUMN_STATUS);
        }
        enforceAllowedValues(values, Downloads.Impl.COLUMN_DESTINATION,
                Downloads.Impl.DESTINATION_CACHE_PARTITION_PURGEABLE,
                Downloads.Impl.DESTINATION_FILE_URI,
                Downloads.Impl.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD);

        if (getContext().checkCallingOrSelfPermission(Downloads.Impl.PERMISSION_NO_NOTIFICATION) == PackageManager.PERMISSION_GRANTED) {
            enforceAllowedValues(values, Downloads.Impl.COLUMN_VISIBILITY,
                    Request.VISIBILITY_HIDDEN,
                    Request.VISIBILITY_VISIBLE,
                    Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
                    Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
        } else {
            enforceAllowedValues(values, Downloads.Impl.COLUMN_VISIBILITY,
                    Request.VISIBILITY_VISIBLE,
                    Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
                    Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
        }

        // remove the rest of the columns that are allowed (with any value)
        values.remove(Downloads.Impl.COLUMN_URI);
        values.remove(Downloads.Impl.COLUMN_TITLE);
        values.remove(Downloads.Impl.COLUMN_DESCRIPTION);
        values.remove(Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS);
        values.remove(Downloads.Impl.COLUMN_BIG_PICTURE);
        values.remove(Downloads.Impl.COLUMN_MIME_TYPE);
        values.remove(Downloads.Impl.COLUMN_FILE_NAME_HINT); // checked later in insert()
        values.remove(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES);
        values.remove(Downloads.Impl.COLUMN_ALLOW_ROAMING);
        values.remove(Downloads.Impl.COLUMN_ALLOW_METERED);
        values.remove(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI);
        values.remove(Downloads.Impl.COLUMN_MEDIA_SCANNED);
        Iterator<Map.Entry<String, Object>> iterator = values.valueSet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next().getKey();
            if (key.startsWith(Downloads.Impl.RequestHeaders.INSERT_KEY_PREFIX)) {
                iterator.remove();
            }
        }

        // any extra columns are extraneous and disallowed
        if (values.size() > 0) {
            StringBuilder error = new StringBuilder("Invalid columns in request: ");
            boolean first = true;
            for (Map.Entry<String, Object> entry : values.valueSet()) {
                if (!first) {
                    error.append(", ");
                }
                error.append(entry.getKey());
            }
            throw new SecurityException(error.toString());
        }
    }

    /**
     * Remove column from values, and throw a SecurityException if the value isn't within the
     * specified allowedValues.
     */
    private void enforceAllowedValues(ContentValues values, String column, Object... allowedValues) {
        Object value = values.get(column);
        values.remove(column);
        for (Object allowedValue : allowedValues) {
            if (value == null && allowedValue == null) {
                return;
            }
            if (value != null && value.equals(allowedValue)) {
                return;
            }
        }
        throw new SecurityException("Invalid value for " + column + ": " + value);
    }

    /**
     * Starts a database query
     */
    @Override
    public Cursor query(final Uri uri, String[] projection,
                        final String selection, final String[] selectionArgs,
                        final String sort) {

        Helpers.validateSelection(selection, sAppReadableColumnsSet);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int match = sURIMatcher.match(uri);
        if (match == -1) {
            Log.v("querying unknown URI: " + uri);
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (match == REQUEST_HEADERS_URI) {
            if (projection != null || selection != null || sort != null) {
                throw new UnsupportedOperationException("Request header queries do not support "
                        + "projections, selections or sorting");
            }
            return queryRequestHeaders(db, uri);
        }

        SqlSelection fullSelection = getWhereClause(uri, selection, selectionArgs, match);

        if (shouldRestrictVisibility()) {
            if (projection == null) {
                projection = sAppReadableColumnsArray.clone();
            } else {
                // check the validity of the columns in projection 
                for (int i = 0; i < projection.length; ++i) {
                    if (!sAppReadableColumnsSet.contains(projection[i]) &&
                            !downloadManagerColumnsList.contains(projection[i])) {
                        throw new IllegalArgumentException(
                                "column " + projection[i] + " is not allowed in queries");
                    }
                }
            }

            for (int i = 0; i < projection.length; i++) {
                final String newColumn = sColumnsMap.get(projection[i]);
                if (newColumn != null) {
                    projection[i] = newColumn;
                }
            }
        }

        logVerboseQueryInfo(projection, selection, selectionArgs, sort, db);

        Cursor ret = db.query(DB_TABLE, projection, fullSelection.getSelection(),
                fullSelection.getParameters(), null, null, sort);

        if (ret != null) {
            ret.setNotificationUri(getContext().getContentResolver(), uri);
            Log.v("created cursor " + ret + " on behalf of " + Binder.getCallingPid());
        } else {
            Log.v("query failed in downloads database");
        }

        return ret;
    }

    private void logVerboseQueryInfo(String[] projection, final String selection,
                                     final String[] selectionArgs, final String sort, SQLiteDatabase db) {
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        sb.append("starting query, database is ");
        if (db != null) {
            sb.append("not ");
        }
        sb.append("null; ");
        if (projection == null) {
            sb.append("projection is null; ");
        } else if (projection.length == 0) {
            sb.append("projection is empty; ");
        } else {
            for (int i = 0; i < projection.length; ++i) {
                sb.append("projection[");
                sb.append(i);
                sb.append("] is ");
                sb.append(projection[i]);
                sb.append("; ");
            }
        }
        sb.append("selection is ");
        sb.append(selection);
        sb.append("; ");
        if (selectionArgs == null) {
            sb.append("selectionArgs is null; ");
        } else if (selectionArgs.length == 0) {
            sb.append("selectionArgs is empty; ");
        } else {
            for (int i = 0; i < selectionArgs.length; ++i) {
                sb.append("selectionArgs[");
                sb.append(i);
                sb.append("] is ");
                sb.append(selectionArgs[i]);
                sb.append("; ");
            }
        }
        sb.append("sort is ");
        sb.append(sort);
        sb.append(".");
        Log.v(sb.toString());
    }

    private String getDownloadIdFromUri(final Uri uri) {
        return uri.getPathSegments().get(1);
    }

    /**
     * Insert request headers for a download into the DB.
     */
    private void insertRequestHeaders(SQLiteDatabase db, long downloadId, ContentValues values) {
        ContentValues rowValues = new ContentValues();
        rowValues.put(Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID, downloadId);
        for (Map.Entry<String, Object> entry : values.valueSet()) {
            String key = entry.getKey();
            if (key.startsWith(Downloads.Impl.RequestHeaders.INSERT_KEY_PREFIX)) {
                String headerLine = entry.getValue().toString();
                if (!headerLine.contains(":")) {
                    throw new IllegalArgumentException("Invalid HTTP header line: " + headerLine);
                }
                String[] parts = headerLine.split(":", 2);
                rowValues.put(Downloads.Impl.RequestHeaders.COLUMN_HEADER, parts[0].trim());
                rowValues.put(Downloads.Impl.RequestHeaders.COLUMN_VALUE, parts[1].trim());
                db.insert(Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE, null, rowValues);
            }
        }
    }

    /**
     * Handle a query for the custom request headers registered for a download.
     */
    private Cursor queryRequestHeaders(SQLiteDatabase db, Uri uri) {
        String where = Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID + "="
                + getDownloadIdFromUri(uri);
        String[] projection = new String[]{Downloads.Impl.RequestHeaders.COLUMN_HEADER,
                Downloads.Impl.RequestHeaders.COLUMN_VALUE};
        return db.query(Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE, projection, where,
                null, null, null, null);
    }

    /**
     * Delete request headers for downloads matching the given query.
     */
    private void deleteRequestHeaders(SQLiteDatabase db, String where, String[] whereArgs) {
        String[] projection = new String[]{Downloads.Impl._ID};
        Cursor cursor = db.query(DB_TABLE, projection, where, whereArgs, null, null, null, null);
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String idWhere = Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID + "=" + id;
                db.delete(Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE, idWhere, null);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * @return true if we should restrict the columns readable by this caller
     */
    private boolean shouldRestrictVisibility() {
        int callingUid = Binder.getCallingUid();
        return Binder.getCallingPid() != Process.myPid() &&
                callingUid != mSystemUid &&
                callingUid != mDefContainerUid;
    }

    /**
     * Updates a row in the database
     */
    @Override
    public int update(final Uri uri, final ContentValues values, final String where, final String[] whereArgs) {

        Helpers.validateSelection(where, sAppReadableColumnsSet);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count;
        boolean startService = false;

        if (values.containsKey(Downloads.Impl.COLUMN_DELETED)) {
            if (values.getAsInteger(Downloads.Impl.COLUMN_DELETED) == 1) {
                // some rows are to be 'deleted'. need to start DownloadService.
                startService = true;
            }
        }

        ContentValues filteredValues;
        if (Binder.getCallingPid() != Process.myPid()) {
            filteredValues = new ContentValues();
            copyString(Downloads.Impl.COLUMN_APP_DATA, values, filteredValues);
            copyInteger(Downloads.Impl.COLUMN_VISIBILITY, values, filteredValues);
            Integer i = values.getAsInteger(Downloads.Impl.COLUMN_CONTROL);
            if (i != null) {
                filteredValues.put(Downloads.Impl.COLUMN_CONTROL, i);
                startService = true;
            }

            copyInteger(Downloads.Impl.COLUMN_CONTROL, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_TITLE, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_MEDIAPROVIDER_URI, values, filteredValues);
            copyString(Downloads.Impl.COLUMN_DESCRIPTION, values, filteredValues);
            copyInteger(Downloads.Impl.COLUMN_DELETED, values, filteredValues);
        } else {
            filteredValues = values;
            String filename = values.getAsString(Downloads.Impl._DATA);
            if (filename != null) {
                Cursor c = query(uri, new String[]{Downloads.Impl.COLUMN_TITLE}, null, null, null);
                if (!c.moveToFirst() || c.getString(0).isEmpty()) {
                    values.put(Downloads.Impl.COLUMN_TITLE, new File(filename).getName());
                }
                c.close();
            }

            Integer status = values.getAsInteger(Downloads.Impl.COLUMN_STATUS);
            boolean isRestart = status != null && status == Downloads.Impl.STATUS_PENDING;
            boolean isUserBypassingSizeLimit =
                    values.containsKey(Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT);
            if (isRestart || isUserBypassingSizeLimit) {
                startService = true;
            }
        }

        int match = sURIMatcher.match(uri);
        switch (match) {
            case MY_DOWNLOADS:
            case MY_DOWNLOADS_ID:
            case ALL_DOWNLOADS:
            case ALL_DOWNLOADS_ID:
                SqlSelection selection = getWhereClause(uri, where, whereArgs, match);
                if (filteredValues.size() > 0) {
                    count = db.update(DB_TABLE, filteredValues, selection.getSelection(),
                            selection.getParameters());
                } else {
                    count = 0;
                }
                break;

            default:
                Log.d("updating unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot update URI: " + uri);
        }

        notifyContentChanged(uri, match);
        if (startService) {
            Context context = getContext();
            context.startService(new Intent(context, DownloadService.class));
        }
        return count;
    }

    /**
     * Notify of a change through both URIs (/my_downloads and /all_downloads)
     *
     * @param uri      either URI for the changed download(s)
     * @param uriMatch the match ID from {@link #sURIMatcher}
     */
    private void notifyContentChanged(final Uri uri, int uriMatch) {
        Long downloadId = null;
        if (uriMatch == MY_DOWNLOADS_ID || uriMatch == ALL_DOWNLOADS_ID) {
            downloadId = Long.parseLong(getDownloadIdFromUri(uri));
        }
        for (Uri uriToNotify : BASE_URIS) {
            if (downloadId != null) {
                uriToNotify = ContentUris.withAppendedId(uriToNotify, downloadId);
            }
            getContext().getContentResolver().notifyChange(uriToNotify, null);
        }
    }

    private SqlSelection getWhereClause(final Uri uri, final String where, final String[] whereArgs,
                                        int uriMatch) {
        SqlSelection selection = new SqlSelection();
        selection.appendClause(where, whereArgs);
        if (uriMatch == MY_DOWNLOADS_ID || uriMatch == ALL_DOWNLOADS_ID ||
                uriMatch == PUBLIC_DOWNLOAD_ID) {
            selection.appendClause(Downloads.Impl._ID + " = ?", getDownloadIdFromUri(uri));
        }
        if ((uriMatch == MY_DOWNLOADS || uriMatch == MY_DOWNLOADS_ID)
                && getContext().checkCallingPermission(Downloads.Impl.PERMISSION_ACCESS_ALL)
                != PackageManager.PERMISSION_GRANTED) {
            selection.appendClause(
                    Constants.UID + "= ? OR " + Downloads.Impl.COLUMN_OTHER_UID + "= ?",
                    Binder.getCallingUid(), Binder.getCallingUid());
        }
        return selection;
    }

    /**
     * Deletes a row in the database
     */
    @Override
    public int delete(final Uri uri, final String where,
                      final String[] whereArgs) {

        Helpers.validateSelection(where, sAppReadableColumnsSet);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        int match = sURIMatcher.match(uri);
        switch (match) {
            case MY_DOWNLOADS:
            case MY_DOWNLOADS_ID:
            case ALL_DOWNLOADS:
            case ALL_DOWNLOADS_ID:
                SqlSelection selection = getWhereClause(uri, where, whereArgs, match);
                deleteRequestHeaders(db, selection.getSelection(), selection.getParameters());
                count = db.delete(DB_TABLE, selection.getSelection(), selection.getParameters());
                break;

            default:
                Log.d("deleting unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot delete URI: " + uri);
        }
        notifyContentChanged(uri, match);
        return count;
    }

    /**
     * Remotely opens a file
     */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        logVerboseOpenFileInfo(uri, mode);

        Cursor cursor = query(uri, new String[]{"_data"}, null, null, null);
        String path;
        try {
            int count = (cursor != null) ? cursor.getCount() : 0;
            if (count != 1) {
                // If there is not exactly one result, throw an appropriate exception.
                if (count == 0) {
                    throw new FileNotFoundException("No entry for " + uri);
                }
                throw new FileNotFoundException("Multiple items at " + uri);
            }

            cursor.moveToFirst();
            path = cursor.getString(0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (path == null) {
            throw new FileNotFoundException("No filename found.");
        }
        if (!Helpers.isFilenameValid(path, mDownloadsDataDir)) {
            Log.d("INTERNAL FILE DOWNLOAD LOL COMMENTED EXCEPTION");
//            throw new FileNotFoundException("Invalid filename: " + path);
        }
        if (!"r".equals(mode)) {
            throw new FileNotFoundException("Bad mode for " + uri + ": " + mode);
        }

        ParcelFileDescriptor ret = ParcelFileDescriptor.open(new File(path),
                ParcelFileDescriptor.MODE_READ_ONLY);

        if (ret == null) {
            Log.v("couldn't open file");
            throw new FileNotFoundException("couldn't open file");
        }
        return ret;
    }

    @Override
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        Log.e("I want dump, but nothing to dump into");
    }

    private void logVerboseOpenFileInfo(Uri uri, String mode) {
        Log.v("openFile uri: " + uri + ", mode: " + mode
                + ", uid: " + Binder.getCallingUid());
        Cursor cursor = query(Downloads.Impl.CONTENT_URI,
                new String[]{"_id"}, null, null, "_id");
        if (cursor == null) {
            Log.v("null cursor in openFile");
        } else {
            if (!cursor.moveToFirst()) {
                Log.v("empty cursor in openFile");
            } else {
                do {
                    Log.v("row " + cursor.getInt(0) + " available");
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        cursor = query(uri, new String[]{"_data"}, null, null, null);
        if (cursor == null) {
            Log.v("null cursor in openFile");
        } else {
            if (!cursor.moveToFirst()) {
                Log.v("empty cursor in openFile");
            } else {
                String filename = cursor.getString(0);
                Log.v("filename in openFile: " + filename);
                if (new java.io.File(filename).isFile()) {
                    Log.v("file exists in openFile");
                }
            }
            cursor.close();
        }
    }

    private static final void copyInteger(String key, ContentValues from, ContentValues to) {
        Integer i = from.getAsInteger(key);
        if (i != null) {
            to.put(key, i);
        }
    }

    private static final void copyBoolean(String key, ContentValues from, ContentValues to) {
        Boolean b = from.getAsBoolean(key);
        if (b != null) {
            to.put(key, b);
        }
    }

    private static final void copyString(String key, ContentValues from, ContentValues to) {
        String s = from.getAsString(key);
        if (s != null) {
            to.put(key, s);
        }
    }

    private static final void copyStringWithDefault(String key, ContentValues from,
                                                    ContentValues to, String defaultValue) {
        copyString(key, from, to);
        if (!to.containsKey(key)) {
            to.put(key, defaultValue);
        }
    }
}
