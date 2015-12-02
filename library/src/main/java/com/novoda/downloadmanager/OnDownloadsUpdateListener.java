package com.novoda.downloadmanager;

import com.novoda.downloadmanager.domain.Download;

import java.util.List;

public interface OnDownloadsUpdateListener {

    void onDownloadsUpdate(List<Download> downloads);

}
