package com.novoda.downloadmanager.lib;

import android.database.Cursor;

final class Cursors {

    private Cursors() {
        // no instances
    }

    public static int getInt(Cursor c, String column) {
        return c.getInt(columnIndexFor(c, column));
    }

    public static long getLong(Cursor c, String column) {
        return c.getLong(columnIndexFor(c, column));
    }

    public static String getString(Cursor c, String column) {
        return c.getString(columnIndexFor(c, column));
    }

    private static int columnIndexFor(Cursor c, String column) {
        return c.getColumnIndexOrThrow(column);
    }

}
