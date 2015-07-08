package com.novoda.downloadmanager.lib;

final class DownloadsDestination {

    /**
     * This download will be saved to the external storage. This is the
     * default behavior, and should be used for any file that the user
     * can freely access, copy, delete. Even with that destination,
     * unencrypted DRM files are saved in secure internal storage.
     * Downloads to the external destination only write files for which
     * there is a registered handler. The resulting files are accessible
     * by filename to all applications.
     */
    public static final int DESTINATION_EXTERNAL = 0;
    /**
     * This download will be saved to the download manager's private
     * partition. This is the behavior used by applications that want to
     * download private files that are used and deleted soon after they
     * get downloaded. All file types are allowed, and only the initiating
     * application can access the file (indirectly through a content
     * provider). This requires the
     * android.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED permission.
     */
    public static final int DESTINATION_CACHE_PARTITION = 1;
    /**
     * This download will be saved to the download manager's private
     * partition and will be purged as necessary to make space. This is
     * for private files (similar to CACHE_PARTITION) that aren't deleted
     * immediately after they are used, and are kept around by the download
     * manager as long as space is available.
     */
    public static final int DESTINATION_CACHE_PARTITION_PURGEABLE = 2;
    /**
     * This download will be saved to the download manager's private
     * partition, as with DESTINATION_CACHE_PARTITION, but the download
     * will not proceed if the user is on a roaming data connection.
     */
    public static final int DESTINATION_CACHE_PARTITION_NOROAMING = 3;
    /**
     * This download will be saved to the location given by the file URI in
     * {@link DownloadsColumns#COLUMN_FILE_NAME_HINT}.
     */
    public static final int DESTINATION_FILE_URI = 4;
    /**
     * This download will be saved to the system cache ("/cache")
     * partition. This option is only used by system apps and so it requires
     * android.permission.ACCESS_CACHE_FILESYSTEM permission.
     */
    public static final int DESTINATION_SYSTEMCACHE_PARTITION = 5;
    /**
     * This download was completed by the caller (i.e., NOT downloadmanager)
     * and caller wants to have this download displayed in Downloads App.
     */
    public static final int DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD = 6;
    /**
     * URI segment to access a publicly accessible downloaded file
     */
    public static final String PUBLICLY_ACCESSIBLE_DOWNLOADS_URI_SEGMENT = "public_downloads";

    private DownloadsDestination() {
        // non-instantiable class
    }
}
