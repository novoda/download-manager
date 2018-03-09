package com.novoda.downloadmanager;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.util.List;

public interface LiteDownloadManagerCommands {

    void download(Batch batch);

    void pause(DownloadBatchId downloadBatchId);

    void resume(DownloadBatchId downloadBatchId);

    void delete(DownloadBatchId downloadBatchId);

    void addDownloadBatchCallback(DownloadBatchStatusCallback downloadBatchCallback);

    void removeDownloadBatchCallback(DownloadBatchStatusCallback downloadBatchCallback);

    void submitAllStoredDownloads(AllStoredDownloadsSubmittedCallback callback);

    @WorkerThread
    List<DownloadBatchStatus> getAllDownloadBatchStatuses();

    void getAllDownloadBatchStatuses(AllBatchStatusesCallback callback);

    @Nullable
    @WorkerThread
    DownloadFileStatus getDownloadFileStatusWithMatching(DownloadBatchId downloadBatchId, DownloadFileId downloadFileId);

    void getDownloadFileStatusWithMatching(DownloadBatchId downloadBatchId, DownloadFileId downloadFileId, DownloadFileStatusCallback callback);

    void updateAllowedConnectionType(ConnectionType allowedConnectionType);

    File getDownloadsDir();
}
