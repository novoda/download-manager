package com.novoda.downloadmanager;

import java.util.List;

public interface AllBatchStatusesCallback {

    void onReceived(List<com.novoda.downloadmanager.DownloadBatchStatus> downloadBatchStatuses);
}
