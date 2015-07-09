package com.novoda.downloadmanager.lib;

final class DownloadsPermission {

    /**
     * The permission to access the download manager
     */
    public static final String PERMISSION_ACCESS = "android.permission.ACCESS_DOWNLOAD_MANAGER";
    /**
     * The permission to access the download manager's advanced functions
     */
    public static final String PERMISSION_ACCESS_ADVANCED = "android.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED";
    /**
     * The permission to access the all the downloads in the manager.
     */
    public static final String PERMISSION_ACCESS_ALL = "android.permission.ACCESS_ALL_DOWNLOADS";
    /**
     * The permission to directly access the download manager's cache
     * directory
     */
    public static final String PERMISSION_CACHE = "android.permission.ACCESS_CACHE_FILESYSTEM";
    /**
     * The permission to send broadcasts on download completion
     */
    public static final String PERMISSION_SEND_INTENTS = "android.permission.SEND_DOWNLOAD_COMPLETED_INTENTS";
    /**
     * The permission to download files to the cache partition that won't be automatically
     * purged when space is needed.
     */
    public static final String PERMISSION_CACHE_NON_PURGEABLE = "android.permission.DOWNLOAD_CACHE_NON_PURGEABLE";
    /**
     * The permission to download files without any system notification being shown.
     */
    public static final String PERMISSION_NO_NOTIFICATION = "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION";

    private DownloadsPermission() {
        // non-instantiable class
    }
}
