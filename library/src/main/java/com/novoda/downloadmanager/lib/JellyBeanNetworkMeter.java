package com.novoda.downloadmanager.lib;

import android.annotation.TargetApi;
import android.net.ConnectivityManager;
import android.os.Build;

class JellyBeanNetworkMeter implements NetworkMeter {

    private final ConnectivityManager conn;

    public JellyBeanNetworkMeter(ConnectivityManager conn) {
        this.conn = conn;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean isActiveNetworkMetered() {
        return conn.isActiveNetworkMetered();
    }

}
