package com.novoda.downloadmanager;

import android.net.Uri;

public enum WatchType {
    STATUS_CHANGE {
        @Override
        Uri toUri() {
            return Provider.DOWNLOAD_STATUS_UPDATE;
        }
    },
    PROGRESS {
        @Override
        Uri toUri() {
            return Provider.DOWNLOAD_PROGRESS_UPDATE;
        }
    };

    abstract Uri toUri();
}
