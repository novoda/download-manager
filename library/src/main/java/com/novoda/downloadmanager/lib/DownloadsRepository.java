package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DownloadsRepository {

    private final ContentResolver contentResolver;
    private final DownloadInfoCreator downloadInfoCreator;
    private final Map<Long, FileDownloadInfo> currentFileInfos = new HashMap<>();

    public DownloadsRepository(ContentResolver contentResolver, DownloadInfoCreator downloadInfoCreator) {
        this.contentResolver = contentResolver;
        this.downloadInfoCreator = downloadInfoCreator;
    }

    public List<FileDownloadInfo> getAllDownloads() {
        Cursor downloadsCursor = contentResolver.query(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, null, null, null, null);
        try {
            FileDownloadInfo.Reader reader = new FileDownloadInfo.Reader(contentResolver, downloadsCursor);

            while (downloadsCursor.moveToNext()) {
                FileDownloadInfo downloadInfo = downloadInfoCreator.create(reader);
                if (currentFileInfos.containsKey(downloadInfo.getId())) {
                    // We have to do an in-place update whilst the DownloadFileInfo
                    // object contains the download thread - creating new objects prevents pause/resume working
                    FileDownloadInfo currentInfo = currentFileInfos.get(downloadInfo.getId());
                    reader.updateFromDatabase(currentInfo);
                } else {
                    currentFileInfos.put(downloadInfo.getId(), downloadInfo);
                }
            }

            return new ArrayList<>(currentFileInfos.values());
        } finally {
            downloadsCursor.close();
        }
    }

    public FileDownloadInfo getDownloadFor(long id) {
        Uri uri = ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id);
        Cursor downloadsCursor = contentResolver.query(uri, null, null, null, null);
        try {
            downloadsCursor.moveToFirst();
            FileDownloadInfo.Reader reader = new FileDownloadInfo.Reader(contentResolver, downloadsCursor);
            return downloadInfoCreator.create(reader);
        } finally {
            downloadsCursor.close();
        }
    }

    interface DownloadInfoCreator {
        FileDownloadInfo create(FileDownloadInfo.Reader reader);
    }

}
