package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

class DownloadsRepository {

    private final ContentResolver contentResolver;
    private final DownloadInfoCreator downloadInfoCreator;
    private final DownloadsUriProvider downloadsUriProvider;

    public DownloadsRepository(ContentResolver contentResolver, DownloadInfoCreator downloadInfoCreator, DownloadsUriProvider downloadsUriProvider) {
        this.contentResolver = contentResolver;
        this.downloadInfoCreator = downloadInfoCreator;
        this.downloadsUriProvider = downloadsUriProvider;
    }

    public List<FileDownloadInfo> getAllDownloads() {
        Cursor downloadsCursor = contentResolver.query(downloadsUriProvider.getAllDownloadsUri(), null, null, null, null);
        try {
            List<FileDownloadInfo> downloads = new ArrayList<>();
            FileDownloadInfo.Reader reader = new FileDownloadInfo.Reader(contentResolver, downloadsCursor);

            while (downloadsCursor.moveToNext()) {
                downloads.add(downloadInfoCreator.create(reader));
            }

            return downloads;
        } finally {
            downloadsCursor.close();
        }
    }

    interface DownloadInfoCreator {
        FileDownloadInfo create(FileDownloadInfo.Reader reader);
    }

}
