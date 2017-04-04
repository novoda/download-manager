package com.novoda.downloadmanager;

import android.content.ContentValues;
import android.net.Uri;

import com.novoda.downloadmanager.demo.simple.DB;

import novoda.lib.sqliteprovider.provider.SQLiteContentProviderImpl;

class Provider extends SQLiteContentProviderImpl {

    private static final String AUTHORITY = "content://" + Reflector.reflectAuthority();

    public static final Uri REQUEST = buildUri(DB.Tables.Request);
    public static final Uri DOWNLOAD = buildUri(DB.Tables.Download);
    public static final Uri FILE = buildUri(DB.Tables.File);
    public static final Uri DOWNLOAD_WITH_SIZE = buildUri(DB.Tables.DownloadsWithSize);

    public static final Uri DOWNLOAD_STATUS_UPDATE = buildUri("DOWNLOAD_STATUS_UPDATE");
    public static final Uri DOWNLOAD_PROGRESS_UPDATE = buildUri("DOWNLOAD_PROGRESS_UPDATE");
    public static final Uri DOWNLOAD_SERVICE_UPDATE = buildUri("DOWNLOAD_SERVICE_UPDATE");

    private static Uri buildUri(String tableOrView) {
        return Uri.parse(AUTHORITY).buildUpon().appendPath(tableOrView).build();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int update = super.update(uri, values, selection, selectionArgs);
        notifyObservers(uri);
        return update;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri insert = super.insert(uri, values);
        notifyObservers(uri);
        return insert;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int delete = super.delete(uri, selection, selectionArgs);
        notifyObservers(uri);
        return delete;
    }

    private void notifyObservers(Uri uri) {
        if (FILE.equals(uri)) {
            notifyUriChange(DOWNLOAD_PROGRESS_UPDATE);
        }

        if (DOWNLOAD.equals(uri)) {
            notifyUriChange(DOWNLOAD_STATUS_UPDATE);
            notifyUriChange(DOWNLOAD_PROGRESS_UPDATE);
            notifyUriChange(DOWNLOAD_SERVICE_UPDATE);
        }
    }

}
