package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.text.TextUtils;

import com.novoda.notils.logger.simple.Log;

import java.io.File;
import java.util.Map;
import java.util.Set;

class DownloadDeleter {

    private final ContentResolver resolver;

    public DownloadDeleter(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public void deleteFileAndDatabaseRow(FileDownloadInfo info) {
        deleteFileAndMediaReference(info);
        resolver.delete(info.getAllDownloadsUri(), null, null);
    }

    public void deleteFileAndMediaReference(FileDownloadInfo info) {
        if (!TextUtils.isEmpty(info.getMediaProviderUri())) {
            resolver.delete(Uri.parse(info.getMediaProviderUri()), null, null);
        }

        if (!TextUtils.isEmpty(info.getFileName())) {
            deleteFileIfExists(info.getFileName());
            ContentValues blankData = new ContentValues();
            blankData.put(DownloadsColumns.COLUMN_DATA, (String) null);
            resolver.update(info.getAllDownloadsUri(), blankData, null, null);
            info.setFileName(null);
        }
    }

    public void cleanUpStaleDownloadsThatDisappeared(Set<Long> staleIds, Map<Long, FileDownloadInfo> downloads) {
        for (Long id : staleIds) {
            deleteDownloadLocked(id, downloads);
        }
    }

    /**
     * Removes the local copy of the info about a download.
     */
    private void deleteDownloadLocked(long id, Map<Long, FileDownloadInfo> downloads) {
        FileDownloadInfo info = downloads.get(id);
        if (info.getStatus() == DownloadsStatus.STATUS_RUNNING) {
            info.setStatus(DownloadsStatus.STATUS_CANCELED);
        }
        if (info.getDestination() != DownloadsDestination.DESTINATION_EXTERNAL && info.getFileName() != null) {
            Log.d("deleteDownloadLocked() deleting " + info.getFileName());
            deleteFileIfExists(info.getFileName());
        }
        downloads.remove(info.getId());
    }

    private void deleteFileIfExists(String path) {
        if (!TextUtils.isEmpty(path)) {
            Log.d("deleteFileIfExists() deleting " + path);
            final File file = new File(path);
            if (file.exists() && !file.delete()) {
                Log.w("file: '" + path + "' couldn't be deleted");
            }
        }
    }

}
