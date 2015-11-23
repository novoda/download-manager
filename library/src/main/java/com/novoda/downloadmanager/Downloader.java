package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.novoda.downloadmanager.domain.Download;
import com.novoda.downloadmanager.domain.DownloadId;
import com.novoda.downloadmanager.domain.DownloadRequest;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;

public class Downloader {

    private final DownloadHandler downloadHandler;
    private final Context context;
    private final Pauser pauser;

    public static Downloader from(Context context) {
        Context applicationContext = context.getApplicationContext();
        ContentResolver contentResolver = applicationContext.getContentResolver();
        DatabaseInteraction databaseInteraction = new DatabaseInteraction(contentResolver);
        ContentLengthFetcher contentLengthFetcher = new ContentLengthFetcher(new OkHttpClient());
        DownloadHandler downloadHandler = new DownloadHandler(databaseInteraction, contentLengthFetcher);
        Pauser pauser = new Pauser(LocalBroadcastManager.getInstance(context));
        return new Downloader(downloadHandler, applicationContext, pauser);
    }

    public Downloader(DownloadHandler downloadHandler, Context context, Pauser pauser) {
        this.downloadHandler = downloadHandler;
        this.context = context;
        this.pauser = pauser;
    }

    public DownloadId createDownloadId() {
        return downloadHandler.createDownloadId();
    }

    public void submit(DownloadRequest downloadRequest) {
        context.startService(new Intent(context, Service.class));
        downloadHandler.submitRequest(downloadRequest);
    }

    public void pause(DownloadId downloadId) {
        pauser.requestPause(downloadId);
    }

    public void resume(DownloadId downloadId) {
        // todo
    }

    public List<Download> getAllDownloads() {
        return downloadHandler.getAllDownloads();
    }

    // todo listening

}
