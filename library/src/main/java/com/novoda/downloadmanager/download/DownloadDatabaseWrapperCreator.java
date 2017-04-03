package com.novoda.downloadmanager.download;

import android.content.ContentResolver;

public class DownloadDatabaseWrapperCreator {

    public static DownloadDatabaseWrapper create(ContentResolver contentResolver) {
        DatabaseInteraction databaseInteraction = new DatabaseInteraction(contentResolver);
        return new DownloadDatabaseWrapper(databaseInteraction);
    }

}
