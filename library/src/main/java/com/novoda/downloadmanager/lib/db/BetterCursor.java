package com.novoda.downloadmanager.lib.db;

import android.database.Cursor;

public class BetterCursor extends CursorWrapper {

    public static BetterCursor wrap(Cursor cursor) {
        return new BetterCursor(cursor);
    }

    BetterCursor(Cursor cursor) {
        super(cursor);
    }

    public int getInt(String columnName) {
        return getInt(getColumnIndexOrThrow(columnName));
    }

    public long getLong(String column) {
        return getLong(getColumnIndexOrThrow(column));
    }

    public String getString(String column) {
        return getString(getColumnIndexOrThrow(column));
    }

}
