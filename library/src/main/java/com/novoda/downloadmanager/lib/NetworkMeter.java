package com.novoda.downloadmanager.lib;

import android.net.ConnectivityManager;
import android.os.Build;

public interface NetworkMeter {
    boolean isActiveNetworkMetered();

    public static class Factory {
        public static NetworkMeter get(ConnectivityManager connectivityManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return new JellyBeanNetworkMeter(connectivityManager);
            } else {
                return new SupportNetworkMeter();
            }
        }

    }
}
