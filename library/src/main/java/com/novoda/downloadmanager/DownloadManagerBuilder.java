package com.novoda.downloadmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.notils.logger.simple.Log;

/**
 * A client can specify whether the downloads are allowed to proceed by implementing
 * {@link com.novoda.downloadmanager.lib.DownloadClientReadyChecker} on your Application class
 *
 */
public class DownloadManagerBuilder {

    private final Context context;

    private boolean verboseLogging;

    DownloadManagerBuilder(Context context) {
        this.context = context;
    }

    public static DownloadManagerBuilder from(Context context) {
        return new DownloadManagerBuilder(context.getApplicationContext());
    }

    public DownloadManagerBuilder withVerboseLogging() {
        this.verboseLogging = true;
        return this;
    }

    public DownloadManager build() {
        ContentResolver contentResolver = context.getContentResolver();
        DownloadManager downloadManager = new DownloadManager(context, contentResolver, verboseLogging);
        updateOnNetworkChanges(downloadManager);
        return downloadManager;
    }

    private void updateOnNetworkChanges(final DownloadManager downloadManager) {
        if (deviceDoesNotSupportConnectivityChangesBroadcast()) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkRequest networkRequest = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    Log.v("network is active now from inside the download manager");
                    downloadManager.forceStart();
                }
            });
        }
    }

    private boolean deviceDoesNotSupportConnectivityChangesBroadcast() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }
}
