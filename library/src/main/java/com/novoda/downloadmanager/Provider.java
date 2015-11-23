package com.novoda.downloadmanager;

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
    public void notifyUriChange(Uri uri) {
        // todo, add uris which don't notify
        super.notifyUriChange(uri);
    }
}