package com.novoda.downloadmanager;

public interface LiteDownloadManagerCommands {

    void download(Batch batch);

    void pause(DownloadBatchId downloadBatchId);

    void resume(DownloadBatchId downloadBatchId);

    void delete(DownloadBatchId downloadBatchId);

    void addDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback);

    void removeDownloadBatchCallback(DownloadBatchCallback downloadBatchCallback);

    void getAllDownloadBatchStatuses(AllBatchStatusesCallback callback);

    void submitAllStoredDownloads(AllStoredDownloadsSubmittedCallback callback);

    void getDownloadFilePath(String uri, DownloadFilePathCallback callback);
}
