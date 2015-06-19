package com.novoda.downloadmanager.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    private static final int DB_VERSION = 110;
    private final String tableName;

    public DatabaseHelper(final Context context, String dbName, String tableName) {
        super(context, dbName, null, DB_VERSION);
        this.tableName = tableName;
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
                addColumn(db, tableName, Downloads.Impl.COLUMN_ALLOW_ROAMING, "INTEGER NOT NULL DEFAULT 0");
                addColumn(db, tableName, Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES, "INTEGER NOT NULL DEFAULT 0");
                break;

            case 103:
                addColumn(db, tableName, Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, "INTEGER NOT NULL DEFAULT 1");
                makeCacheDownloadsInvisible(db);
                break;

            case 104:
                addColumn(db, tableName, Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT, "INTEGER NOT NULL DEFAULT 0");
                break;

            case 105:
                fillNullValues(db);
                break;

            case 106:
                addColumn(db, tableName, Downloads.Impl.COLUMN_MEDIAPROVIDER_URI, "TEXT");
                addColumn(db, tableName, Downloads.Impl.COLUMN_DELETED, "BOOLEAN NOT NULL DEFAULT 0");
                break;

            case 107:
                addColumn(db, tableName, Downloads.Impl.COLUMN_ERROR_MSG, "TEXT");
                break;

            case 108:
                addColumn(db, tableName, Downloads.Impl.COLUMN_ALLOW_METERED, "INTEGER NOT NULL DEFAULT 1");
                break;

            case 109:
                addColumn(db, tableName, Downloads.Impl.COLUMN_BIG_PICTURE, "TEXT");
                break;
            case 110:
                addColumn(db, tableName, Downloads.Impl.COLUMN_BATCH_ID, "INTEGER");
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
        db.update(tableName, values, column + " is null", null);
        values.clear();
    }

    /**
     * Set all existing downloads to the cache partition to be invisible in the downloads UI.
     */
    private void makeCacheDownloadsInvisible(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, false);
        String cacheSelection = Downloads.Impl.COLUMN_DESTINATION + " != " + Downloads.Impl.DESTINATION_EXTERNAL;
        db.update(tableName, values, cacheSelection, null);
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
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            db.execSQL("CREATE TABLE " + tableName + "(" +
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
        db.execSQL(
                "CREATE TABLE " + Downloads.Impl.RequestHeaders.HEADERS_DB_TABLE + "(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        Downloads.Impl.RequestHeaders.COLUMN_DOWNLOAD_ID + " INTEGER NOT NULL," +
                        Downloads.Impl.RequestHeaders.COLUMN_HEADER + " TEXT NOT NULL," +
                        Downloads.Impl.RequestHeaders.COLUMN_VALUE + " TEXT NOT NULL" +
                        ");");
    }
}
