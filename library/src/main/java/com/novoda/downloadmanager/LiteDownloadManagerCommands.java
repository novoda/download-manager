package com.novoda.downloadmanager;

import android.support.annotation.WorkerThread;

import java.util.List;

public interface LiteDownloadManagerCommands {

    void download(Batch batch);

    void pause(DownloadBatchId downloadBatchId);

    void resume(DownloadBatchId downloadBatchId);

    void delete(DownloadBatchId downloadBatchId);

    void addDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback);

    void removeDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback);

    @WorkerThread
    List<DownloadBatchStatus> getAllDownloadBatchStatuses();

    void getAllDownloadBatchStatuses(AllBatchStatusesCallback callback);

    void submitAllStoredDownloads(AllStoredDownloadsSubmittedCallback callback);

    void getDownloadStatusWithMatching(DownloadFileId downloadFileId, DownloadFileStatusCallback callback);
}
