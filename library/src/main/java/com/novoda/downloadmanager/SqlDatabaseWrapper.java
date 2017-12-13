package com.novoda.downloadmanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlDatabaseWrapper {

    private final SQLiteDatabase sqLiteDatabase;

    public SqlDatabaseWrapper(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
    }

    public boolean isOpen() {
        return sqLiteDatabase.isOpen();
    }

    public Cursor rawQuery(String query) {
        return sqLiteDatabase.rawQuery(query, null);
    }

    public Cursor rawQuery(String query, String selectionArgument, String... selectionArguments) {
        List<String> arguments = new ArrayList<>();
        arguments.add(selectionArgument);
        arguments.addAll(Arrays.asList(selectionArguments));

        return sqLiteDatabase.rawQuery(query, arguments.toArray(new String[arguments.size()]));
    }

    public void close() {
        sqLiteDatabase.close();
    }

    public void deleteDatabase() {
        File outputFile = new File(sqLiteDatabase.getPath());
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }
}
