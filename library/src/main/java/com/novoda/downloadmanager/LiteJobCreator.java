package com.novoda.downloadmanager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

class LiteJobCreator implements JobCreator {

    static final String TAG = "download-manager-reschedule";

    private final DownloadManager downloadManager;

    LiteJobCreator(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    @Override
    public Job create(String tag) {
        if (tag.equals(TAG)) {
            return new LiteJobDownload(downloadManager);
        }

        return null;
    }
}
