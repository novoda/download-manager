package com.novoda.downloadmanager;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

class ConnectionChecker {

    private static final boolean IS_NOT_CONNECTED_TO_NETWORK_TYPE = false;

    private final ConnectivityManager connectivityManager;
    private ConnectionType allowedConnectionType;

    ConnectionChecker(ConnectivityManager connectivityManager, ConnectionType allowedConnectionType) {
        this.connectivityManager = connectivityManager;
        this.allowedConnectionType = allowedConnectionType;
    }

    boolean isAllowedToDownload() {
        switch (allowedConnectionType) {
            case UNMETERED:
                return isConnectedToWifi();
            case METERED:
                return isConnectedToMobileNetwork();
            default:
                return true;
        }
    }

    void updateAllowedConnectionType(ConnectionType allowedConnectionType) {
        this.allowedConnectionType = allowedConnectionType;
    }

    private boolean isConnectedToMobileNetwork() {
        return isConnectedTo(ConnectivityManager.TYPE_MOBILE);
    }

    private boolean isConnectedToWifi() {
        return isConnectedTo(ConnectivityManager.TYPE_WIFI);
    }

    private boolean isConnectedTo(int networkType) {
        return connectedToNetworkTypeForLollipop(networkType);
    }

    private boolean connectedToNetworkTypeForLollipop(int networkType) {
        Network[] networks = connectivityManager.getAllNetworks();

        for (Network network : networks) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);

            if (networkInfo != null && networkInfo.getType() == networkType) {
                return networkInfo.isConnected();
            }

        }

        return IS_NOT_CONNECTED_TO_NETWORK_TYPE;
    }
}
