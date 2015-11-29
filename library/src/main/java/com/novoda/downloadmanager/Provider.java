package com.novoda.downloadmanager;

import android.content.ContentValues;
import android.net.Uri;

import com.novoda.downloadmanager.demo.simple.DB;

import novoda.lib.sqliteprovider.provider.SQLiteContentProviderImpl;

public class Provider extends SQLiteContentProviderImpl {

    private static final String AUTHORITY = "content://com.novoda.downloadmanager.demo.simple";

    public static final Uri REQUEST = buildUri(DB.Tables.Request);
    public static final Uri DOWNLOAD = buildUri(DB.Tables.Download);
    public static final Uri FILE = buildUri(DB.Tables.File);
    public static final Uri DOWNLOAD_WITH_SIZE = buildUri(DB.Tables.DownloadsWithSize);

    private static Uri buildUri(String tableOrView) {
        return Uri.parse(AUTHORITY).buildUpon().appendPath(tableOrView).build();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int update = super.update(uri, values, selection, selectionArgs);
        notifyViews(uri);
        return update;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri insert = super.insert(uri, values);
        notifyViews(uri);
        return insert;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int delete = super.delete(uri, selection, selectionArgs);
        notifyViews(uri);
        return delete;
    }

    private void notifyViews(Uri uri) {
        if (FILE.equals(uri) || DOWNLOAD.equals(uri)) {
            notifyUriChange(DOWNLOAD_WITH_SIZE);
        }
    }

}