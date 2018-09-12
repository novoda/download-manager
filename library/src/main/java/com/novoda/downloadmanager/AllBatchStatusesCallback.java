package com.novoda.downloadmanager;

import java.util.List;

/**
 * Given to the asynchronous call {@link LiteDownloadManagerCommands#getAllDownloadBatchStatuses(AllBatchStatusesCallback)},
 * to receive a List of the current {@link DownloadBatchStatus} stored by the download-manager.
 */
public interface AllBatchStatusesCallback {

    void onReceived(List<DownloadBatchStatus> downloadBatchStatuses);
}
