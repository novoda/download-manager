package com.novoda.downloadmanager.lib.jobscheduler;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class DownloadManagerJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        if (tag.equals(DownloadJob.TAG)) {
            return new DownloadJob();
        }

        return null;
    }
}
