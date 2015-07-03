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
            db.execSQL("DROP TABLE IF EXISTS " + Downloads.Impl.DOWNLOADS_TABLE_NAME);
            db.execSQL("CREATE TABLE " + Downloads.Impl.DOWNLOADS_TABLE_NAME + "(" +
                    Downloads.Impl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Downloads.Impl.COLUMN_URI + " TEXT, " +
                    Constants.RETRY_AFTER_X_REDIRECT_COUNT + " INTEGER, " +
                    Downloads.Impl.COLUMN_APP_DATA + " TEXT, " +
                    Downloads.Impl.COLUMN_NO_INTEGRITY + " BOOLEAN, " +
                    Downloads.Impl.COLUMN_FILE_NAME_HINT + " TEXT, " +
                    Downloads.Impl._DATA + " TEXT, " +
                    Downloads.Impl.COLUMN_MIME_TYPE + " TEXT, " +
                    Downloads.Impl.COLUMN_DESTINATION + " INTEGER, " +
                    Constants.NO_SYSTEM_FILES + " BOOLEAN, " +
                    Downloads.Impl.COLUMN_CONTROL + " INTEGER, " +
                    Downloads.Impl.COLUMN_STATUS + " INTEGER, " +
                    Downloads.Impl.COLUMN_FAILED_CONNECTIONS + " INTEGER, " +
                    Downloads.Impl.COLUMN_LAST_MODIFICATION + " BIGINT, " +
                    Downloads.Impl.COLUMN_NOTIFICATION_CLASS + " TEXT, " +
                    Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS + " TEXT, " +
                    Downloads.Impl.COLUMN_COOKIE_DATA + " TEXT, " +
                    Downloads.Impl.COLUMN_USER_AGENT + " TEXT, " +
                    Downloads.Impl.COLUMN_REFERER + " TEXT, " +
                    Downloads.Impl.COLUMN_TOTAL_BYTES + " INTEGER NOT NULL DEFAULT -1, " +
                    Downloads.Impl.COLUMN_CURRENT_BYTES + " INTEGER NOT NULL DEFAULT 0, " +
                    Constants.ETAG + " TEXT, " +
                    Constants.UID + " INTEGER, " +
                    Downloads.Impl.COLUMN_OTHER_UID + " INTEGER, " +
                    Downloads.Impl.COLUMN_ALLOW_ROAMING + " INTEGER NOT NULL DEFAULT 0, " +
                    Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES + " INTEGER NOT NULL DEFAULT 0, " +
                    Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI + " INTEGER NOT NULL DEFAULT 1, " +
                    Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT + " INTEGER NOT NULL DEFAULT 0, " +
                    Downloads.Impl.COLUMN_MEDIAPROVIDER_URI + " TEXT, " +
                    Downloads.Impl.COLUMN_DELETED + " BOOLEAN NOT NULL DEFAULT 0, " +
                    Downloads.Impl.COLUMN_ERROR_MSG + " TEXT, " +
                    Downloads.Impl.COLUMN_ALLOW_METERED + " INTEGER NOT NULL DEFAULT 1, " +
                    Downloads.Impl.COLUMN_BATCH_ID + " INTEGER, " +
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
        values.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, false);
        String cacheSelection = Downloads.Impl.COLUMN_DESTINATION + " != " + Downloads.Impl.DESTINATION_EXTERNAL;
        db.update(Downloads.Impl.DOWNLOADS_TABLE_NAME, values, cacheSelection, null);
    }

    private void createHeadersTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE);
        db.execSQL(
                "CREATE TABLE " + Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE + "(" +
                        Downloads.Impl.RequestHeaders._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID + " INTEGER NOT NULL," +
                        Downloads.Impl.RequestHeaders.COLUMN_HEADER + " TEXT NOT NULL," +
                        Downloads.Impl.RequestHeaders.COLUMN_VALUE + " TEXT NOT NULL" +
                        ");");
    }

    private void createBatchesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + Downloads.Impl.Batches.BATCHES_TABLE_NAME);
        db.execSQL(
                "CREATE TABLE " + Downloads.Impl.Batches.BATCHES_TABLE_NAME + "(" +
                        Downloads.Impl.Batches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Downloads.Impl.Batches.COLUMN_TITLE + " TEXT NOT NULL," +
                        Downloads.Impl.Batches.COLUMN_DESCRIPTION + " TEXT," +
                        Downloads.Impl.Batches.COLUMN_BIG_PICTURE + " TEXT," +
                        Downloads.Impl.Batches.COLUMN_STATUS + " INTEGER," +
                        Downloads.Impl.Batches.COLUMN_VISIBILITY + " INTEGER," +
                        Downloads.Impl.Batches.COLUMN_DELETED + " BOOLEAN NOT NULL DEFAULT 0" +
                        ");");
    }

    private void createDownloadsByBatchView(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS " + Downloads.Impl.VIEW_NAME_DOWNLOADS_BY_BATCH);
        db.execSQL("CREATE VIEW " + Downloads.Impl.VIEW_NAME_DOWNLOADS_BY_BATCH
                        + " AS SELECT DISTINCT "
                        + projectionFrom(DOWNLOAD_BY_BATCH_VIEW_COLUMNS)
                        + " FROM " + Downloads.Impl.DOWNLOADS_TABLE_NAME
                        + " INNER JOIN " + Downloads.Impl.Batches.BATCHES_TABLE_NAME
                        + " ON " + Downloads.Impl.DOWNLOADS_TABLE_NAME + "." + Downloads.Impl.COLUMN_BATCH_ID
                        + " = " + Downloads.Impl.Batches.BATCHES_TABLE_NAME + "." + Downloads.Impl.Batches._ID + ";"
        );
    }

    /**
     * columns to request from DownloadProvider.
     */
    public static final String[] DOWNLOAD_BY_BATCH_VIEW_COLUMNS = new String[]{
            Downloads.Impl.DOWNLOADS_TABLE_NAME + "." + Downloads.Impl._ID + " AS _id",
            Downloads.Impl._DATA,
            Downloads.Impl.COLUMN_MEDIAPROVIDER_URI,
            Downloads.Impl.COLUMN_DESTINATION,
            Downloads.Impl.COLUMN_URI,
            Downloads.Impl.COLUMN_STATUS,
            Downloads.Impl.DOWNLOADS_TABLE_NAME + "." + Downloads.Impl.COLUMN_DELETED,
            Downloads.Impl.COLUMN_FILE_NAME_HINT,
            Downloads.Impl.COLUMN_MIME_TYPE,
            Downloads.Impl.COLUMN_TOTAL_BYTES,
            Downloads.Impl.COLUMN_LAST_MODIFICATION,
            Downloads.Impl.COLUMN_CURRENT_BYTES,
            Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS,
            Downloads.Impl.COLUMN_BATCH_ID,
            Downloads.Impl.Batches.COLUMN_TITLE,
            Downloads.Impl.Batches.COLUMN_DESCRIPTION,
            Downloads.Impl.Batches.COLUMN_BIG_PICTURE,
            Downloads.Impl.Batches.COLUMN_VISIBILITY,
            Downloads.Impl.Batches.COLUMN_STATUS,
            Downloads.Impl.Batches.BATCHES_TABLE_NAME + "." + Downloads.Impl.Batches.COLUMN_DELETED,
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
