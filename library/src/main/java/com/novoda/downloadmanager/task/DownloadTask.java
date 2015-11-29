package com.novoda.downloadmanager.task;

import android.util.Log;

import com.novoda.downloadmanager.DownloadHandler;
import com.novoda.downloadmanager.Pauser;
import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.domain.DownloadId;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;

public class DownloadTask implements Runnable {

    private final DownloadId downloadId;
    private final DownloadHandler downloadHandler;
    private final Pauser pauser;
    private final OkHttpClient httpClient = new OkHttpClient();

    private boolean downloadPaused;

    public DownloadTask(DownloadId download, DownloadHandler downloadHandler, Pauser pauser) {
        this.downloadId = download;
        this.downloadHandler = downloadHandler;
        this.pauser = pauser;
    }

    @Override
    public void run() {
        Log.e("!!!", "Download thread started");

        Download download = downloadHandler.getDownload(downloadId);

        if (downloadStatusIsIgnored(download)) {
            Log.e("!!!", "Download status is ignored, bailing out");
            // why did the thread start?! we're out of sync
            return;
        }

        boolean isDownloadStartingForTheFirstTime = download.getCurrentSize() == 0;
        if (isDownloadStartingForTheFirstTime) {
//                  todo  handleBatchStartingForTheFirstTime(downloadBatch);
        }

        downloadHandler.setDownloadRunning(downloadId);

        pauser.listenForPause(downloadId, onDownloadPaused);
        download(download);
        pauser.stopListeningForPause();

        Log.e("!!!", "thread finished : " + download.getId());
    }

    private boolean downloadStatusIsIgnored(Download download) {
        return download.getStage().noActionRequired();
    }

    private void download(Download download) {
        try {
            FileDownloader fileDownloader = new FileDownloader(httpClient, downloadHandler, pausedProvider);
            startDownload(download, fileDownloader);
            downloadHandler.syncDownloadStatus(downloadId);
        } catch (IOException e) {
            downloadHandler.setDownloadFailed(downloadId);
            e.printStackTrace();
        } catch (FileDownloader.PausedFlowException e) {
            downloadHandler.setDownloadPaused(downloadId);
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
