package com.novoda.downloadmanager.lib;

import android.net.Uri;

public class DownloadsUriProvider {

    private final String authority;

    public static DownloadsUriProvider newInstance() {
        return new DownloadsUriProvider(DownloadProvider.AUTHORITY);
    }

    DownloadsUriProvider(String authority) {
        this.authority = authority;
    }

    /**
     * The content URI for accessing publicly accessible downloads (i.e., it requires no
     * permissions to access this downloaded file)
     */
    public Uri getPublicityAccessibleDownloadsUri() {
        return Uri.parse(authority + "/" + Downloads.Impl.PUBLICLY_ACCESSIBLE_DOWNLOADS_URI_SEGMENT);
    }

    /**
     * The content:// URI to access downloads and their batch data.
     */
    public Uri getDownloadsByBatchUri() {
        return Uri.parse(authority + "/downloads_by_batch");
    }

    /**
     * The content URI for accessing all downloads across all UIDs (requires the
     * ACCESS_ALL_DOWNLOADS permission).
     */
    public Uri getAllDownloadsContentUri() {
        return Uri.parse(authority + "/all_downloads");
    }

    /**
     * The content:// URI to access downloads owned by the caller's UID.
     */
    public Uri getBatchContentUri() {
        return Uri.parse(authority + "/batches");
    }

    /**
     * The content:// URI to access downloads owned by the caller's UID.
     */
    public Uri getContentUri() {
        return Uri.parse(authority + "/my_downloads");
    }
}
