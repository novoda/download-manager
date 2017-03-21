package com.novoda.downloadmanager.lib;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

class DownloadManagerJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        if (tag.equals(DownloadJob.TAG)) {
            return new DownloadJob();
        }

        return null;
    }
}
