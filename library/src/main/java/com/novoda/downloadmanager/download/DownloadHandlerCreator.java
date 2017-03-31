package com.novoda.downloadmanager.download;

import android.content.ContentResolver;

public class DownloadHandlerCreator {

    public static DownloadHandler create(ContentResolver contentResolver) {
        DatabaseInteraction databaseInteraction = new DatabaseInteraction(contentResolver);
        return new DownloadHandler(databaseInteraction);
    }

}
