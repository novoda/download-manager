package com.novoda.downloadmanager.download.task;

import android.util.Log;

import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.download.DownloadHandler;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FileDownloader {

    private final OkHttpClient httpClient;
    private final DownloadHandler downloadHandler;
    private final PausedProvider pausedProvider;

    public FileDownloader(OkHttpClient httpClient, DownloadHandler downloadHandler, PausedProvider pausedProvider) {
        this.httpClient = httpClient;
        this.downloadHandler = downloadHandler;
        this.pausedProvider = pausedProvider;
    }

    public void downloadFile(DownloadFile file) throws IOException {
        File downloadedFile = new File(file.getLocalUri());

        Log.e("!!!", "download file : " + file.getLocalUri() + " : " + (downloadedFile.exists() ? downloadedFile.length() : "0"));

        if (!downloadedFile.exists()) {
            downloadedFile.createNewFile();
        }
        Request.Builder requestBuilder = createRequest(file, downloadedFile.length());
        Call call = httpClient.newCall(requestBuilder.build());
        Response response = call.execute();

        Log.e("!!!", "response code : " + response.code());

        InputStream in = response.body().byteStream();
        OutputStream out = new FileOutputStream(downloadedFile, downloadedFile.length() != 0);

        DataWriter checkedWriter = new CheckedWriter(getSpaceVerifier(), out);

        DataWriter dataWriter = new NotifierWriter(checkedWriter, file, check, downloadHandler);
        DataTransferer dataTransferer = new RegularDataTransferer(dataWriter);

        State state = new State();
        state.currentBytes = downloadedFile.length();

        try {
            State endState = dataTransferer.transferData(state, in);
        } finally {
            syncDatabase(file, downloadedFile.length());
        }
    }

    private Request.Builder createRequest(DownloadFile file, long fileSize) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(file.getUri());

        if (file.currentSize() > 0) {
            requestBuilder.addHeader("Range", "bytes=" + fileSize + "-" + file.totalSize());
        }
        return requestBuilder;
    }

    private SpaceVerifier getSpaceVerifier() {
        return new SpaceVerifier() {
            @Override
            public void verifySpacePreemptively(int count) {
                // todo
            }

            @Override
            public void verifySpace(int count) {
                // todo
            }
        };
    }

    private final NotifierWriter.WriteChunkListener check = new NotifierWriter.WriteChunkListener() {
        @Override
        public void chunkWritten(DownloadFile file) {
            if (pausedProvider.isPaused()) {
                throw new PausedFlowException();
            }
        }
    };

    private void syncDatabase(DownloadFile file, long fileSize) {
        if (file.totalSize() == fileSize) {
            downloadHandler.updateFile(file, DownloadFile.FileStatus.COMPLETE, fileSize);
        } else {
            downloadHandler.updateFile(file, DownloadFile.FileStatus.INCOMPLETE, fileSize);
        }
    }

    interface PausedProvider {
        boolean isPaused();
    }

    static class PausedFlowException extends RuntimeException {

    }

}
