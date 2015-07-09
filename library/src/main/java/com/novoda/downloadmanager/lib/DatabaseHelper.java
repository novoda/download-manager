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
            db.execSQL("DROP TABLE IF EXISTS " + DownloadsContract.DOWNLOADS_TABLE_NAME);
            db.execSQL("CREATE TABLE " + DownloadsContract.DOWNLOADS_TABLE_NAME + "(" +
                    DownloadsContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DownloadsContract.COLUMN_URI + " TEXT, " +
                    Constants.RETRY_AFTER_X_REDIRECT_COUNT + " INTEGER, " +
                    DownloadsContract.COLUMN_APP_DATA + " TEXT, " +
                    DownloadsContract.COLUMN_NO_INTEGRITY + " BOOLEAN, " +
                    DownloadsContract.COLUMN_FILE_NAME_HINT + " TEXT, " +
                    DownloadsContract.COLUMN_DATA + " TEXT, " +
                    DownloadsContract.COLUMN_MIME_TYPE + " TEXT, " +
                    DownloadsContract.COLUMN_DESTINATION + " INTEGER, " +
                    Constants.NO_SYSTEM_FILES + " BOOLEAN, " +
                    DownloadsContract.COLUMN_CONTROL + " INTEGER, " +
                    DownloadsContract.COLUMN_STATUS + " INTEGER, " +
                    DownloadsContract.COLUMN_FAILED_CONNECTIONS + " INTEGER, " +
                    DownloadsContract.COLUMN_LAST_MODIFICATION + " BIGINT, " +
                    DownloadsContract.COLUMN_NOTIFICATION_CLASS + " TEXT, " +
                    DownloadsContract.COLUMN_NOTIFICATION_EXTRAS + " TEXT, " +
                    DownloadsContract.COLUMN_COOKIE_DATA + " TEXT, " +
                    DownloadsContract.COLUMN_USER_AGENT + " TEXT, " +
                    DownloadsContract.COLUMN_REFERER + " TEXT, " +
                    DownloadsContract.COLUMN_TOTAL_BYTES + " INTEGER NOT NULL DEFAULT -1, " +
                    DownloadsContract.COLUMN_CURRENT_BYTES + " INTEGER NOT NULL DEFAULT 0, " +
                    Constants.ETAG + " TEXT, " +
                    Constants.UID + " INTEGER, " +
                    DownloadsContract.COLUMN_OTHER_UID + " INTEGER, " +
                    DownloadsContract.COLUMN_ALLOW_ROAMING + " INTEGER NOT NULL DEFAULT 0, " +
                    DownloadsContract.COLUMN_ALLOWED_NETWORK_TYPES + " INTEGER NOT NULL DEFAULT 0, " +
                    DownloadsContract.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI + " INTEGER NOT NULL DEFAULT 1, " +
                    DownloadsContract.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT + " INTEGER NOT NULL DEFAULT 0, " +
                    DownloadsContract.COLUMN_MEDIAPROVIDER_URI + " TEXT, " +
                    DownloadsContract.COLUMN_DELETED + " BOOLEAN NOT NULL DEFAULT 0, " +
                    DownloadsContract.COLUMN_ERROR_MSG + " TEXT, " +
                    DownloadsContract.COLUMN_ALLOW_METERED + " INTEGER NOT NULL DEFAULT 1, " +
                    DownloadsContract.COLUMN_BATCH_ID + " INTEGER, " +
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
        values.put(DownloadsContract.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, false);
        String cacheSelection = DownloadsContract.COLUMN_DESTINATION + " != " + DownloadsDestination.DESTINATION_EXTERNAL;
        db.update(DownloadsContract.DOWNLOADS_TABLE_NAME, values, cacheSelection, null);
    }

    private void createHeadersTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + RequestHeadersContract.HEADERS_DB_TABLE);
        db.execSQL(
                "CREATE TABLE " + RequestHeadersContract.HEADERS_DB_TABLE + "(" +
                        RequestHeadersContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        RequestHeadersContract.COLUMN_DOWNLOAD_ID + " INTEGER NOT NULL," +
                        RequestHeadersContract.COLUMN_HEADER + " TEXT NOT NULL," +
                        RequestHeadersContract.COLUMN_VALUE + " TEXT NOT NULL" +
                        ");");
    }

    private void createBatchesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + BatchesContract.BATCHES_TABLE_NAME);
        db.execSQL(
                "CREATE TABLE " + BatchesContract.BATCHES_TABLE_NAME + "(" +
                        BatchesContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        BatchesContract.COLUMN_TITLE + " TEXT NOT NULL," +
                        BatchesContract.COLUMN_DESCRIPTION + " TEXT," +
                        BatchesContract.COLUMN_BIG_PICTURE + " TEXT," +
                        BatchesContract.COLUMN_STATUS + " INTEGER," +
                        BatchesContract.COLUMN_VISIBILITY + " INTEGER," +
                        BatchesContract.COLUMN_DELETED + " BOOLEAN NOT NULL DEFAULT 0, " +
                        BatchesContract.COLUMN_TOTAL_BYTES + " INTEGER NOT NULL DEFAULT -1, " +
                        BatchesContract.COLUMN_CURRENT_BYTES + " INTEGER NOT NULL DEFAULT 0 " +
                        ");");
    }

    private void createDownloadsByBatchView(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS " + DownloadsByBatchContract.VIEW_NAME_DOWNLOADS_BY_BATCH);
        db.execSQL("CREATE VIEW " + DownloadsByBatchContract.VIEW_NAME_DOWNLOADS_BY_BATCH
                        + " AS SELECT DISTINCT "
                        + projectionFrom(DOWNLOAD_BY_BATCH_VIEW_COLUMNS)
                        + " FROM " + DownloadsContract.DOWNLOADS_TABLE_NAME
                        + " INNER JOIN " + BatchesContract.BATCHES_TABLE_NAME
                        + " ON " + DownloadsContract.DOWNLOADS_TABLE_NAME + "." + DownloadsContract.COLUMN_BATCH_ID
                        + " = " + BatchesContract.BATCHES_TABLE_NAME + "." + BatchesContract._ID + ";"
        );
    }

    /**
     * columns to request from DownloadProvider.
     */
    public static final String[] DOWNLOAD_BY_BATCH_VIEW_COLUMNS = new String[]{
            DownloadsContract.DOWNLOADS_TABLE_NAME + "." + DownloadsContract._ID + " AS _id ",
            DownloadsContract.COLUMN_DATA,
            DownloadsContract.COLUMN_MEDIAPROVIDER_URI,
            DownloadsContract.COLUMN_DESTINATION,
            DownloadsContract.COLUMN_URI,
            DownloadsContract.COLUMN_STATUS,
            DownloadsContract.DOWNLOADS_TABLE_NAME + "." + DownloadsContract.COLUMN_DELETED,
            DownloadsContract.COLUMN_FILE_NAME_HINT,
            DownloadsContract.COLUMN_MIME_TYPE,
            DownloadsContract.COLUMN_TOTAL_BYTES,
            DownloadsContract.COLUMN_LAST_MODIFICATION,
            DownloadsContract.COLUMN_CURRENT_BYTES,
            DownloadsContract.COLUMN_NOTIFICATION_EXTRAS,
            DownloadsContract.COLUMN_BATCH_ID,
            BatchesContract.COLUMN_TITLE,
            BatchesContract.COLUMN_DESCRIPTION,
            BatchesContract.COLUMN_BIG_PICTURE,
            BatchesContract.COLUMN_VISIBILITY,
            BatchesContract.COLUMN_STATUS,
            BatchesContract.BATCHES_TABLE_NAME + "." + BatchesContract.COLUMN_DELETED,
            BatchesContract.COLUMN_TOTAL_BYTES,
            BatchesContract.COLUMN_CURRENT_BYTES
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
