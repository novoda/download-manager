package com.novoda.downloadmanager.lib;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

class NetworkChecker {

    private final SystemFacade systemFacade;

    NetworkChecker(SystemFacade systemFacade) {
        this.systemFacade = systemFacade;
    }

    /**
     * Returns whether this download is allowed to use the network.
     */
    public FileDownloadInfo.NetworkState checkCanUseNetwork(FileDownloadInfo downloadInfo) {
        final NetworkInfo info = systemFacade.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return FileDownloadInfo.NetworkState.NO_CONNECTION;
        }
        if (NetworkInfo.DetailedState.BLOCKED.equals(info.getDetailedState())) {
            return FileDownloadInfo.NetworkState.BLOCKED;
        }
        if (systemFacade.isNetworkRoaming() && !downloadInfo.allowRoaming()) {
            return FileDownloadInfo.NetworkState.CANNOT_USE_ROAMING;
        }
        if (systemFacade.isActiveNetworkMetered() && !downloadInfo.allowMetered()) {
            return FileDownloadInfo.NetworkState.TYPE_DISALLOWED_BY_REQUESTOR;
        }
        return checkIsNetworkTypeAllowed(downloadInfo, info.getType());
    }

    /**
     * Check if this download can proceed over the given network type.
     *
     * @param networkType a constant from ConnectivityManager.TYPE_*.
     * @return one of the NETWORK_* constants
     */
    private FileDownloadInfo.NetworkState checkIsNetworkTypeAllowed(FileDownloadInfo downloadInfo, int networkType) {
        if (downloadInfo.getTotalBytes() <= 0) {
            return FileDownloadInfo.NetworkState.OK; // we don't know the size yet
        }
        if (networkType == ConnectivityManager.TYPE_WIFI) {
            return FileDownloadInfo.NetworkState.OK; // anything goes over wifi
        }
        Long maxBytesOverMobile = systemFacade.getMaxBytesOverMobile();
        if (maxBytesOverMobile != null && downloadInfo.getTotalBytes() > maxBytesOverMobile) {
            return FileDownloadInfo.NetworkState.UNUSABLE_DUE_TO_SIZE;
        }
        if (downloadInfo.isRecommendedSizeLimitBypassed()) {
            Long recommendedMaxBytesOverMobile = systemFacade.getRecommendedMaxBytesOverMobile();
            if (recommendedMaxBytesOverMobile != null && downloadInfo.getTotalBytes() > recommendedMaxBytesOverMobile) {
                return FileDownloadInfo.NetworkState.RECOMMENDED_UNUSABLE_DUE_TO_SIZE;
            }
        }
        return FileDownloadInfo.NetworkState.OK;
    }

}
