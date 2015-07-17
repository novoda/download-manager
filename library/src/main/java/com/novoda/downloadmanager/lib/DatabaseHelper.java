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

    /**
     * columns to request from DownloadProvider.
     */
    public static final String[] DOWNLOAD_BY_BATCH_VIEW_COLUMNS = new String[]{
            DownloadContract.Downloads.DOWNLOADS_TABLE_NAME + "." + DownloadContract.Downloads._ID + " AS _id ",
            DownloadContract.Downloads.COLUMN_DATA,
            DownloadContract.Downloads.COLUMN_MEDIAPROVIDER_URI,
            DownloadContract.Downloads.COLUMN_DESTINATION,
            DownloadContract.Downloads.COLUMN_URI,
            DownloadContract.Downloads.COLUMN_STATUS,
            DownloadContract.Downloads.DOWNLOADS_TABLE_NAME + "." + DownloadContract.Downloads.COLUMN_DELETED,
            DownloadContract.Downloads.COLUMN_FILE_NAME_HINT,
            DownloadContract.Downloads.COLUMN_MIME_TYPE,
            DownloadContract.Downloads.COLUMN_TOTAL_BYTES,
            DownloadContract.Downloads.DOWNLOADS_TABLE_NAME + "." + DownloadContract.Downloads.COLUMN_LAST_MODIFICATION,
            DownloadContract.Downloads.COLUMN_CURRENT_BYTES,
            DownloadContract.Downloads.COLUMN_NOTIFICATION_EXTRAS,
            DownloadContract.Downloads.DOWNLOADS_TABLE_NAME + "." + DownloadContract.Downloads.COLUMN_EXTRA_DATA,
            DownloadContract.Downloads.COLUMN_BATCH_ID,
            DownloadContract.Batches.COLUMN_TITLE,
            DownloadContract.Batches.COLUMN_DESCRIPTION,
            DownloadContract.Batches.COLUMN_BIG_PICTURE,
            DownloadContract.Batches.COLUMN_VISIBILITY,
            DownloadContract.Batches.COLUMN_STATUS,
            DownloadContract.BatchesWithSizes.VIEW_NAME_BATCHES_WITH_SIZES + "." + DownloadContract.Batches.COLUMN_DELETED,
            DownloadContract.BatchesWithSizes.COLUMN_TOTAL_BYTES,
            DownloadContract.BatchesWithSizes.COLUMN_CURRENT_BYTES
    };

    public static final String[] DOWNLOADS_WITHOUT_PROGRESS_VIEW_COLUMNS = new String[]{
            DownloadContract.Downloads.DOWNLOADS_TABLE_NAME + "." + DownloadContract.Downloads._ID + " AS _id ",
            DownloadContract.Downloads.COLUMN_DATA,
            DownloadContract.Downloads.COLUMN_MEDIAPROVIDER_URI,
            DownloadContract.Downloads.COLUMN_DESTINATION,
            DownloadContract.Downloads.COLUMN_URI,
            DownloadContract.Downloads.COLUMN_STATUS,
            DownloadContract.Downloads.COLUMN_FILE_NAME_HINT,
            DownloadContract.Downloads.COLUMN_MIME_TYPE,
            DownloadContract.Downloads.COLUMN_NOTIFICATION_EXTRAS,
            DownloadContract.Downloads.DOWNLOADS_TABLE_NAME + "." + DownloadContract.Downloads.COLUMN_EXTRA_DATA,
            DownloadContract.Downloads.COLUMN_BATCH_ID,
            DownloadContract.Batches.COLUMN_TITLE,
            DownloadContract.Batches.COLUMN_DESCRIPTION,
            DownloadContract.Batches.COLUMN_BIG_PICTURE,
            DownloadContract.Batches.COLUMN_STATUS
    };

    public static final String[] BATCHES_WITHOUT_PROGRESS_VIEW_COLUMNS = new String[]{
            DownloadContract.Batches.COLUMN_TITLE,
            DownloadContract.Batches.COLUMN_DESCRIPTION,
            DownloadContract.Batches.COLUMN_BIG_PICTURE,
            DownloadContract.Batches.COLUMN_STATUS,
            DownloadContract.Batches.COLUMN_EXTRA_DATA,
            DownloadContract.Batches.COLUMN_LAST_MODIFICATION
    };

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
        createBatchesWithSizesView(db);
        createDownloadsByBatchView(db);
        createDownloadsWithoutProgressView(db);
        createBatchesWithoutProgressView(db);
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
            db.execSQL("DROP TABLE IF EXISTS " + DownloadContract.Downloads.DOWNLOADS_TABLE_NAME);
            db.execSQL(
                    "CREATE TABLE " + DownloadContract.Downloads.DOWNLOADS_TABLE_NAME + "("
                            + DownloadContract.Downloads._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + DownloadContract.Downloads.COLUMN_URI + " TEXT, "
                            + Constants.RETRY_AFTER_X_REDIRECT_COUNT + " INTEGER, "
                            + DownloadContract.Downloads.COLUMN_APP_DATA + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_NO_INTEGRITY + " BOOLEAN, "
                            + DownloadContract.Downloads.COLUMN_FILE_NAME_HINT + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_DATA + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_MIME_TYPE + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_DESTINATION + " INTEGER, "
                            + Constants.NO_SYSTEM_FILES + " BOOLEAN, "
                            + DownloadContract.Downloads.COLUMN_CONTROL + " INTEGER, "
                            + DownloadContract.Downloads.COLUMN_STATUS + " INTEGER, "
                            + DownloadContract.Downloads.COLUMN_FAILED_CONNECTIONS + " INTEGER, "
                            + DownloadContract.Downloads.COLUMN_LAST_MODIFICATION + " BIGINT, "
                            + DownloadContract.Downloads.COLUMN_NOTIFICATION_CLASS + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_NOTIFICATION_EXTRAS + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_COOKIE_DATA + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_USER_AGENT + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_REFERER + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_TOTAL_BYTES + " INTEGER NOT NULL DEFAULT -1, "
                            + DownloadContract.Downloads.COLUMN_CURRENT_BYTES + " INTEGER NOT NULL DEFAULT 0, "
                            + Constants.ETAG + " TEXT, "
                            + Constants.UID + " INTEGER, "
                            + DownloadContract.Downloads.COLUMN_OTHER_UID + " INTEGER, "
                            + DownloadContract.Downloads.COLUMN_ALLOW_ROAMING + " INTEGER NOT NULL DEFAULT 0, "
                            + DownloadContract.Downloads.COLUMN_ALLOWED_NETWORK_TYPES + " INTEGER NOT NULL DEFAULT 0, "
                            + DownloadContract.Downloads.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI + " INTEGER NOT NULL DEFAULT 1, "
                            + DownloadContract.Downloads.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT + " INTEGER NOT NULL DEFAULT 0, "
                            + DownloadContract.Downloads.COLUMN_MEDIAPROVIDER_URI + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_DELETED + " BOOLEAN NOT NULL DEFAULT 0, "
                            + DownloadContract.Downloads.COLUMN_ERROR_MSG + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_ALLOW_METERED + " INTEGER NOT NULL DEFAULT 1, "
                            + DownloadContract.Downloads.COLUMN_BATCH_ID + " INTEGER, "
                            + DownloadContract.Downloads.COLUMN_EXTRA_DATA + " TEXT, "
                            + DownloadContract.Downloads.COLUMN_ALWAYS_RESUME + " INTEGER NOT NULL DEFAULT 0, "
                            + Constants.MEDIA_SCANNED + " BOOLEAN);"
            );
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
        values.put(DownloadContract.Downloads.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, false);
        String cacheSelection = DownloadContract.Downloads.COLUMN_DESTINATION + " != " + DownloadsDestination.DESTINATION_EXTERNAL;
        db.update(DownloadContract.Downloads.DOWNLOADS_TABLE_NAME, values, cacheSelection, null);
    }

    private void createHeadersTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DownloadContract.RequestHeaders.HEADERS_DB_TABLE);
        db.execSQL(
                "CREATE TABLE " + DownloadContract.RequestHeaders.HEADERS_DB_TABLE + "("
                        + DownloadContract.RequestHeaders._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + DownloadContract.RequestHeaders.COLUMN_DOWNLOAD_ID + " INTEGER NOT NULL,"
                        + DownloadContract.RequestHeaders.COLUMN_HEADER + " TEXT NOT NULL,"
                        + DownloadContract.RequestHeaders.COLUMN_VALUE + " TEXT NOT NULL"
                        + ");"
        );
    }

    private void createBatchesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DownloadContract.Batches.BATCHES_TABLE_NAME);
        db.execSQL(
                "CREATE TABLE " + DownloadContract.Batches.BATCHES_TABLE_NAME + "("
                        + DownloadContract.Batches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + DownloadContract.Batches.COLUMN_TITLE + " TEXT NOT NULL,"
                        + DownloadContract.Batches.COLUMN_DESCRIPTION + " TEXT,"
                        + DownloadContract.Batches.COLUMN_BIG_PICTURE + " TEXT,"
                        + DownloadContract.Batches.COLUMN_STATUS + " INTEGER,"
                        + DownloadContract.Batches.COLUMN_VISIBILITY + " INTEGER,"
                        + DownloadContract.Batches.COLUMN_DELETED + " BOOLEAN NOT NULL DEFAULT 0,"
                        + DownloadContract.Batches.COLUMN_EXTRA_DATA + " TEXT,"
                        + DownloadContract.Batches.COLUMN_LAST_MODIFICATION + " TEXT"
                        + ");"
        );
    }

    private void createDownloadsByBatchView(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS " + DownloadContract.DownloadsByBatch.VIEW_NAME_DOWNLOADS_BY_BATCH);
        db.execSQL(
                "CREATE VIEW " + DownloadContract.DownloadsByBatch.VIEW_NAME_DOWNLOADS_BY_BATCH
                        + " AS SELECT DISTINCT "
                        + projectionFrom(DOWNLOAD_BY_BATCH_VIEW_COLUMNS)
                        + " FROM " + DownloadContract.Downloads.DOWNLOADS_TABLE_NAME
                        + " INNER JOIN " + DownloadContract.BatchesWithSizes.VIEW_NAME_BATCHES_WITH_SIZES
                        + " ON " + DownloadContract.Downloads.DOWNLOADS_TABLE_NAME + "." + DownloadContract.Downloads.COLUMN_BATCH_ID
                        + " = " + DownloadContract.BatchesWithSizes.VIEW_NAME_BATCHES_WITH_SIZES + "." + DownloadContract.Batches._ID + ";"
        );
    }

    private void createBatchesWithSizesView(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS " + DownloadContract.BatchesWithSizes.VIEW_NAME_BATCHES_WITH_SIZES);
        db.execSQL(
                "CREATE VIEW " + DownloadContract.BatchesWithSizes.VIEW_NAME_BATCHES_WITH_SIZES
                        + " AS SELECT DISTINCT "
                        + DownloadContract.Batches.BATCHES_TABLE_NAME + ".*, "
                        + DownloadContract.BatchesWithSizes.COLUMN_CURRENT_BYTES + ", "
                        + DownloadContract.BatchesWithSizes.COLUMN_TOTAL_BYTES
                        + " FROM " + DownloadContract.Batches.BATCHES_TABLE_NAME
                        + " INNER JOIN "
                        + "  (SELECT "
                        + "    " + DownloadContract.Downloads.COLUMN_BATCH_ID + ","
                        + "    SUM(" + DownloadContract.Downloads.COLUMN_CURRENT_BYTES + ") AS " + DownloadContract.BatchesWithSizes.COLUMN_CURRENT_BYTES + ","
                        + "    MAX(SUM(" + DownloadContract.Downloads.COLUMN_TOTAL_BYTES + "), -1) AS " + DownloadContract.BatchesWithSizes.COLUMN_TOTAL_BYTES
                        + "    FROM " + DownloadContract.Downloads.DOWNLOADS_TABLE_NAME
                        + "    GROUP BY " + DownloadContract.Downloads.COLUMN_BATCH_ID
                        + "  ) " + DownloadContract.Downloads.DOWNLOADS_TABLE_NAME
                        + " ON " + DownloadContract.Downloads.DOWNLOADS_TABLE_NAME + "." + DownloadContract.Downloads.COLUMN_BATCH_ID
                        + " = " + DownloadContract.Batches.BATCHES_TABLE_NAME + "." + DownloadContract.Batches._ID + ";"
        );
    }

    private void createDownloadsWithoutProgressView(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS " + DownloadContract.DownloadsWithoutProgress.VIEW_NAME_DOWNLOADS_WITHOUT_PROGRESS);
        db.execSQL(
                "CREATE VIEW " + DownloadContract.DownloadsWithoutProgress.VIEW_NAME_DOWNLOADS_WITHOUT_PROGRESS
                        + " AS SELECT DISTINCT "
                        + projectionFrom(DOWNLOADS_WITHOUT_PROGRESS_VIEW_COLUMNS)
                        + " FROM " + DownloadContract.Downloads.DOWNLOADS_TABLE_NAME
                        + " INNER JOIN " + DownloadContract.Batches.BATCHES_TABLE_NAME
                        + " ON " + DownloadContract.Downloads.DOWNLOADS_TABLE_NAME + "." + DownloadContract.Downloads.COLUMN_BATCH_ID
                        + " = " + DownloadContract.Batches.BATCHES_TABLE_NAME + "." + DownloadContract.Batches._ID + ";"
        );
    }

    private void createBatchesWithoutProgressView(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS " + DownloadContract.BatchesWithoutProgress.VIEW_NAME_BATCHES_WITHOUT_PROGRESS);
        db.execSQL(
                "CREATE VIEW " + DownloadContract.BatchesWithoutProgress.VIEW_NAME_BATCHES_WITHOUT_PROGRESS
                        + " AS SELECT DISTINCT "
                        + projectionFrom(BATCHES_WITHOUT_PROGRESS_VIEW_COLUMNS)
                        + " FROM " + DownloadContract.Batches.BATCHES_TABLE_NAME
                        + ";"
        );
    }

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
