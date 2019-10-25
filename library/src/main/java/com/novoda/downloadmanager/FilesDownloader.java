package com.novoda.downloadmanager;

import java.util.List;

interface FilesDownloader {
    void download(List<DownloadFile> downloadFiles, DownloadBatchStatusCallback statusCallback, DownloadFile.Callback fileCallback);
}
