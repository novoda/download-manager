package com.novoda.downloadmanager;

import android.content.ContentResolver;

class DownloadDatabaseWrapperCreator {

    public static DownloadDatabaseWrapper create(ContentResolver contentResolver) {
        DatabaseInteraction databaseInteraction = new DatabaseInteraction(contentResolver);
        return new DownloadDatabaseWrapper(databaseInteraction);
    }

}
