package com.novoda.downloadmanager.lib;

import android.net.ConnectivityManager;
import android.os.Build;

interface NetworkMeter {
    boolean isActiveNetworkMetered();

    class Factory {
        public static NetworkMeter get(ConnectivityManager connectivityManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return new JellyBeanNetworkMeter(connectivityManager);
            } else {
                return new SupportNetworkMeter();
            }
        }

    }
}
