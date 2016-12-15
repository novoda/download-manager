package com.novoda.downloadmanager.download;

import android.content.ContentResolver;

import com.squareup.okhttp.OkHttpClient;

public class DownloadHandlerCreator {

    public static DownloadHandler create(ContentResolver contentResolver) {
        DatabaseInteraction databaseInteraction = new DatabaseInteraction(contentResolver);
        ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher(new OkHttpClient());
        return new DownloadHandler(databaseInteraction, contentLengthFetcher);
    }

}
