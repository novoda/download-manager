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

    public void deleteFileAndDatabaseRow(DownloadInfo info) {
        deleteFileAndMediaReference(info);
        resolver.delete(info.getAllDownloadsUri(), null, null);
    }

    public void deleteFileAndMediaReference(DownloadInfo info) {
        if (!TextUtils.isEmpty(info.mMediaProviderUri)) {
            resolver.delete(Uri.parse(info.mMediaProviderUri), null, null);
        }

        if (!TextUtils.isEmpty(info.mFileName)) {
            deleteFileIfExists(info.mFileName);
            ContentValues blankData = new ContentValues();
            blankData.put(Downloads.Impl._DATA, (String) null);
            resolver.update(info.getAllDownloadsUri(), blankData, null, null);
            info.mFileName = null;
        }
    }

    public void cleanUpStaleDownloadsThatDisappeared(Set<Long> staleIds, Map<Long, DownloadInfo> downloads) {
        for (Long id : staleIds) {
            deleteDownloadLocked(id, downloads);
        }
    }

    /**
     * Removes the local copy of the info about a download.
     */
    private void deleteDownloadLocked(long id, Map<Long, DownloadInfo> downloads) {
        DownloadInfo info = downloads.get(id);
        if (info.mStatus == Downloads.Impl.STATUS_RUNNING) {
            info.mStatus = Downloads.Impl.STATUS_CANCELED;
        }
        if (info.mDestination != Downloads.Impl.DESTINATION_EXTERNAL && info.mFileName != null) {
            Log.d("deleteDownloadLocked() deleting " + info.mFileName);
            deleteFileIfExists(info.mFileName);
        }
        downloads.remove(info.mId);
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
