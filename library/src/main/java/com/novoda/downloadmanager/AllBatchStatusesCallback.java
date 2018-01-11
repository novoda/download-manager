package com.novoda.downloadmanager;

import java.util.List;

public interface AllBatchStatusesCallback {

    void onReceived(List<DownloadBatchStatus> downloadBatchStatuses);
}
