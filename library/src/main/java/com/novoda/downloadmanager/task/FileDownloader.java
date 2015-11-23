package com.novoda.downloadmanager.task;

import com.novoda.downloadmanager.DownloadHandler;
import com.novoda.downloadmanager.domain.DownloadFile;
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
        downloadedFile.createNewFile();
        Request.Builder requestBuilder = createRequest(file);
        Call call = httpClient.newCall(requestBuilder.build());
        Response response = call.execute();

        InputStream in = response.body().byteStream();
        OutputStream out = new FileOutputStream(downloadedFile);

        DataWriter checkedWriter = new CheckedWriter(getSpaceVerifier(), out);

        DataWriter dataWriter = new NotifierWriter(checkedWriter, file, check, downloadHandler);
        DataTransferer dataTransferer = new RegularDataTransferer(dataWriter);

        State state = new State();
        State endState = dataTransferer.transferData(state, in);

        syncDatabase(file, endState);
    }

    private Request.Builder createRequest(DownloadFile file) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(file.getUri());

        if (file.currentSize() > 0) {
            requestBuilder.addHeader("Range", "bytes=" + file.currentSize() + "-");
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

    private void syncDatabase(DownloadFile file, State endState) {
        downloadHandler.updateFile(file, DownloadFile.FileStatus.COMPLETE, endState.currentBytes);
    }

    interface PausedProvider {
        boolean isPaused();
    }

    static class PausedFlowException extends RuntimeException {

    }

}
