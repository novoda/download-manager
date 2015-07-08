package com.novoda.downloadmanager.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.novoda.notils.logger.simple.Log;

/**
 * Creates and updated database on demand when opening it.
 * Helper class to create database the first time the provider is
 * initialized and upgrade it when a new version of the provider needs
 * an updated version of the database.
 */
final class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Current database version
     */
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context, String dbName) {
        super(context, dbName, null, DB_VERSION);
    }

    /**
     * Creates database the first time we try to open it.
     */
    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        Log.v("populating new database");
        createDownloadsTable(db);
        createHeadersTable(db);
        createBatchesTable(db);
        createDownloadsByBatchView(db);
        makeCacheDownloadsInvisible(db);
    }

    /**
     * Updates the database format when a content provider is used
     * with a database that was created with a different format.
     * <p/>
     * Note: to support downgrades, creating a table should always drop it first if it already
     * exists.
     */
    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, final int newVersion) {
        // no upgrade path yet
    }

    /**
     * Creates the table that'll hold the download information.
     */
    private void createDownloadsTable(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + DownloadsTables.DOWNLOADS_TABLE_NAME);
            db.execSQL("CREATE TABLE " + DownloadsTables.DOWNLOADS_TABLE_NAME + "(" +
                    Downloads.Impl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DownloadsColumns.COLUMN_URI + " TEXT, " +
                    Constants.RETRY_AFTER_X_REDIRECT_COUNT + " INTEGER, " +
                    DownloadsColumns.COLUMN_APP_DATA + " TEXT, " +
                    DownloadsColumns.COLUMN_NO_INTEGRITY + " BOOLEAN, " +
                    DownloadsColumns.COLUMN_FILE_NAME_HINT + " TEXT, " +
                    DownloadsColumns.COLUMN_DATA + " TEXT, " +
                    DownloadsColumns.COLUMN_MIME_TYPE + " TEXT, " +
                    DownloadsColumns.COLUMN_DESTINATION + " INTEGER, " +
                    Constants.NO_SYSTEM_FILES + " BOOLEAN, " +
                    DownloadsColumns.COLUMN_CONTROL + " INTEGER, " +
                    DownloadsColumns.COLUMN_STATUS + " INTEGER, " +
                    DownloadsColumns.COLUMN_FAILED_CONNECTIONS + " INTEGER, " +
                    DownloadsColumns.COLUMN_LAST_MODIFICATION + " BIGINT, " +
                    DownloadsColumns.COLUMN_NOTIFICATION_CLASS + " TEXT, " +
                    DownloadsColumns.COLUMN_NOTIFICATION_EXTRAS + " TEXT, " +
                    DownloadsColumns.COLUMN_COOKIE_DATA + " TEXT, " +
                    DownloadsColumns.COLUMN_USER_AGENT + " TEXT, " +
                    DownloadsColumns.COLUMN_REFERER + " TEXT, " +
                    DownloadsColumns.COLUMN_TOTAL_BYTES + " INTEGER NOT NULL DEFAULT -1, " +
                    DownloadsColumns.COLUMN_CURRENT_BYTES + " INTEGER NOT NULL DEFAULT 0, " +
                    Constants.ETAG + " TEXT, " +
                    Constants.UID + " INTEGER, " +
                    DownloadsColumns.COLUMN_OTHER_UID + " INTEGER, " +
                    DownloadsColumns.COLUMN_ALLOW_ROAMING + " INTEGER NOT NULL DEFAULT 0, " +
                    DownloadsColumns.COLUMN_ALLOWED_NETWORK_TYPES + " INTEGER NOT NULL DEFAULT 0, " +
                    DownloadsColumns.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI + " INTEGER NOT NULL DEFAULT 1, " +
                    DownloadsColumns.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT + " INTEGER NOT NULL DEFAULT 0, " +
                    DownloadsColumns.COLUMN_MEDIAPROVIDER_URI + " TEXT, " +
                    DownloadsColumns.COLUMN_DELETED + " BOOLEAN NOT NULL DEFAULT 0, " +
                    DownloadsColumns.COLUMN_ERROR_MSG + " TEXT, " +
                    DownloadsColumns.COLUMN_ALLOW_METERED + " INTEGER NOT NULL DEFAULT 1, " +
                    DownloadsColumns.COLUMN_BATCH_ID + " INTEGER, " +
                    Constants.MEDIA_SCANNED + " BOOLEAN);");
        } catch (SQLException ex) {
            Log.e("couldn't create table in downloads database");
            throw ex;
        }
    }

    /**
     * Set all existing downloads to the cache partition to be invisible in the downloads UI.
     */
    private void makeCacheDownloadsInvisible(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(DownloadsColumns.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, false);
        String cacheSelection = DownloadsColumns.COLUMN_DESTINATION + " != " + DownloadsDestination.DESTINATION_EXTERNAL;
        db.update(DownloadsTables.DOWNLOADS_TABLE_NAME, values, cacheSelection, null);
    }

    private void createHeadersTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DownloadsColumnsRequestHeaders.HEADERS_DB_TABLE);
        db.execSQL(
                "CREATE TABLE " + DownloadsColumnsRequestHeaders.HEADERS_DB_TABLE + "(" +
                        DownloadsColumnsRequestHeaders._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        DownloadsColumnsRequestHeaders.COLUMN_DOWNLOAD_ID + " INTEGER NOT NULL," +
                        DownloadsColumnsRequestHeaders.COLUMN_HEADER + " TEXT NOT NULL," +
                        DownloadsColumnsRequestHeaders.COLUMN_VALUE + " TEXT NOT NULL" +
                        ");");
    }

    private void createBatchesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DownloadsColumnsBatches.BATCHES_TABLE_NAME);
        db.execSQL(
                "CREATE TABLE " + DownloadsColumnsBatches.BATCHES_TABLE_NAME + "(" +
                        DownloadsColumnsBatches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        DownloadsColumnsBatches.COLUMN_TITLE + " TEXT NOT NULL," +
                        DownloadsColumnsBatches.COLUMN_DESCRIPTION + " TEXT," +
                        DownloadsColumnsBatches.COLUMN_BIG_PICTURE + " TEXT," +
                        DownloadsColumnsBatches.COLUMN_STATUS + " INTEGER," +
                        DownloadsColumnsBatches.COLUMN_VISIBILITY + " INTEGER," +
                        DownloadsColumnsBatches.COLUMN_DELETED + " BOOLEAN NOT NULL DEFAULT 0, " +
                        DownloadsColumnsBatches.COLUMN_TOTAL_BYTES + " INTEGER NOT NULL DEFAULT -1, " +
                        DownloadsColumnsBatches.COLUMN_CURRENT_BYTES + " INTEGER NOT NULL DEFAULT 0 " +
                        ");");
    }

    private void createDownloadsByBatchView(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS " + DownloadsTables.VIEW_NAME_DOWNLOADS_BY_BATCH);
        db.execSQL("CREATE VIEW " + DownloadsTables.VIEW_NAME_DOWNLOADS_BY_BATCH
                        + " AS SELECT DISTINCT "
                        + projectionFrom(DOWNLOAD_BY_BATCH_VIEW_COLUMNS)
                        + " FROM " + DownloadsTables.DOWNLOADS_TABLE_NAME
                        + " INNER JOIN " + DownloadsColumnsBatches.BATCHES_TABLE_NAME
                        + " ON " + DownloadsTables.DOWNLOADS_TABLE_NAME + "." + DownloadsColumns.COLUMN_BATCH_ID
                        + " = " + DownloadsColumnsBatches.BATCHES_TABLE_NAME + "." + DownloadsColumnsBatches._ID + ";"
        );
    }

    /**
     * columns to request from DownloadProvider.
     */
    public static final String[] DOWNLOAD_BY_BATCH_VIEW_COLUMNS = new String[]{
            DownloadsTables.DOWNLOADS_TABLE_NAME + "." + Downloads.Impl._ID + " AS _id ",
            DownloadsColumns.COLUMN_DATA,
            DownloadsColumns.COLUMN_MEDIAPROVIDER_URI,
            DownloadsColumns.COLUMN_DESTINATION,
            DownloadsColumns.COLUMN_URI,
            DownloadsColumns.COLUMN_STATUS,
            DownloadsTables.DOWNLOADS_TABLE_NAME + "." + DownloadsColumns.COLUMN_DELETED,
            DownloadsColumns.COLUMN_FILE_NAME_HINT,
            DownloadsColumns.COLUMN_MIME_TYPE,
            DownloadsColumns.COLUMN_TOTAL_BYTES,
            DownloadsColumns.COLUMN_LAST_MODIFICATION,
            DownloadsColumns.COLUMN_CURRENT_BYTES,
            DownloadsColumns.COLUMN_NOTIFICATION_EXTRAS,
            DownloadsColumns.COLUMN_BATCH_ID,
            DownloadsColumnsBatches.COLUMN_TITLE,
            DownloadsColumnsBatches.COLUMN_DESCRIPTION,
            DownloadsColumnsBatches.COLUMN_BIG_PICTURE,
            DownloadsColumnsBatches.COLUMN_VISIBILITY,
            DownloadsColumnsBatches.COLUMN_STATUS,
            DownloadsColumnsBatches.BATCHES_TABLE_NAME + "." + DownloadsColumnsBatches.COLUMN_DELETED,
            DownloadsColumnsBatches.COLUMN_TOTAL_BYTES,
            DownloadsColumnsBatches.COLUMN_CURRENT_BYTES
    };

    private String projectionFrom(String[] array) {
        if (array == null) {
            return "null";
        }
        if (array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder(array.length * 7);
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", ");
            sb.append(array[i]);
        }
        return sb.toString();
    }
}
