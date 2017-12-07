package com.novoda.downloadmanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SqlDatabaseWrapper {

    private final SQLiteDatabase sqLiteDatabase;

    SqlDatabaseWrapper(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
    }

    Cursor rawQuery(String query) {
        return sqLiteDatabase.rawQuery(query, null);
    }

    Cursor rawQuery(String query, String selectionArgument, String... selectionArguments) {
        List<String> arguments = new ArrayList<>();
        arguments.add(selectionArgument);
        arguments.addAll(Arrays.asList(selectionArguments));

        return sqLiteDatabase.rawQuery(query, arguments.toArray(new String[arguments.size()]));
    }

    void close() {
        sqLiteDatabase.close();
    }
}
