package com.novoda.downloadmanager.lib;

import android.database.Cursor;

final class Cursors {

    private Cursors() {
        // no instances
    }

    public static int getInt(Cursor c, String column) {
        return c.getInt(c.getColumnIndexOrThrow(column));
    }

    public static long getLong(Cursor c, String column) {
        return c.getLong(c.getColumnIndexOrThrow(column));
    }

    public static String getString(Cursor c, String column) {
        return c.getString(c.getColumnIndexOrThrow(column));
    }

}
