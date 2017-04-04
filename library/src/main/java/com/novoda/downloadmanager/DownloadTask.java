package com.novoda.downloadmanager;

import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;

class DownloadTask implements Runnable {

    private final DownloadId downloadId;
    private final DownloadDatabaseWrapper downloadDatabaseWrapper;
    private final Pauser pauser;
    private final OkHttpClient httpClient = new OkHttpClient();

    private boolean downloadPaused;

    public DownloadTask(DownloadId download, DownloadDatabaseWrapper downloadDatabaseWrapper, Pauser pauser) {
        this.downloadId = download;
        this.downloadDatabaseWrapper = downloadDatabaseWrapper;
        this.pauser = pauser;
    }

    @Override
    public void run() {
        Download download = downloadDatabaseWrapper.getDownload(downloadId);

        downloadDatabaseWrapper.setDownloadRunning(downloadId);

        pauser.listenForPause(downloadId, onDownloadPaused);
        download(download);
        pauser.stopListeningForPause();
    }

    private void download(Download download) {
        try {
            FileDownloader fileDownloader = new FileDownloader(httpClient, downloadDatabaseWrapper, pausedProvider);
            startDownload(download, fileDownloader);
            downloadDatabaseWrapper.syncDownloadStatus(downloadId);
        } catch (IOException e) {
            downloadDatabaseWrapper.setDownloadFailed(downloadId);
            e.printStackTrace();
        } catch (FileDownloader.PausedFlowException e) {
            downloadDatabaseWrapper.setDownloadPaused(downloadId);
        }
    }

    private final Pauser.OnPauseListener onDownloadPaused = new Pauser.OnPauseListener() {
        @Override
        public void onDownloadPaused() {
            downloadPaused = true;
        }
    };

    private final FileDownloader.PausedProvider pausedProvider = new FileDownloader.PausedProvider() {
        @Override
        public boolean isPaused() {
            return downloadPaused;
        }
    };

    private void startDownload(Download download, FileDownloader fileDownloader) throws IOException {
        for (DownloadFile file : download.getFiles()) {
            if (fileNeedsDownloading(file)) {
                fileDownloader.downloadFile(file);
            }
        }
    }

    private boolean fileNeedsDownloading(DownloadFile file) {
        return file.getStatus() == DownloadFile.FileStatus.INCOMPLETE;
    }

}
