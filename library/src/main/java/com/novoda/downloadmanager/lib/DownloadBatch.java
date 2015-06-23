package com.novoda.downloadmanager.lib;

import android.content.ContentValues;

import java.util.List;

public class DownloadBatch {

    private final String title;
    private final String description;
    private final String bigPictureUrl;

    public DownloadBatch(String title, String description, String bigPictureUrl) {
        this.title = title;
        this.description = description;
        this.bigPictureUrl = bigPictureUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBigPictureUrl() {
        return bigPictureUrl;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.Batches.COLUMN_TITLE, title);
        values.put(Downloads.Impl.Batches.COLUMN_DESCRIPTION, description);
        values.put(Downloads.Impl.Batches.COLUMN_BIG_PICTURE, bigPictureUrl);
        return values;
    }
}
