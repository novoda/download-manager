package com.novoda.downloadmanager.lib;

import android.net.Uri;

public class DownloadsUriProvider {

    private final Uri publicityAccessibleDownloadsUriSegment;
    private final Uri downloadsByBatchUri;
    private final Uri allDownloadsContentUri;
    private final Uri batchContentUri;
    private final Uri contentUri;

    public static DownloadsUriProvider newInstance() {
        String authority = DownloadProvider.AUTHORITY;

        Uri publicityAccessibleDownloadsUriSegment = Uri.parse(authority + "/" + Downloads.Impl.PUBLICLY_ACCESSIBLE_DOWNLOADS_URI_SEGMENT);
        Uri downloadsByBatchUri = Uri.parse(authority + "/downloads_by_batch");
        Uri allDownloadsContentUri = Uri.parse(authority + "/all_downloads");
        Uri batchContentUri = Uri.parse(authority + "/batches");
        Uri contentUri = Uri.parse(authority + "/my_downloads");

        return new DownloadsUriProvider(
                publicityAccessibleDownloadsUriSegment,
                downloadsByBatchUri,
                allDownloadsContentUri,
                batchContentUri,
                contentUri
        );
    }

    DownloadsUriProvider(Uri publicityAccessibleDownloadsUriSegment, Uri downloadsByBatchUri, Uri allDownloadsContentUri, Uri batchContentUri, Uri contentUri) {
        this.publicityAccessibleDownloadsUriSegment = publicityAccessibleDownloadsUriSegment;
        this.downloadsByBatchUri = downloadsByBatchUri;
        this.allDownloadsContentUri = allDownloadsContentUri;
        this.batchContentUri = batchContentUri;
        this.contentUri = contentUri;
    }

    /**
     * The content URI for accessing publicly accessible downloads (i.e., it requires no
     * permissions to access this downloaded file)
     */
    public Uri getPublicityAccessibleDownloadsUri() {
        return publicityAccessibleDownloadsUriSegment;
    }

    /**
     * The content:// URI to access downloads and their batch data.
     */
    public Uri getDownloadsByBatchUri() {
        return downloadsByBatchUri;
    }

    /**
     * The content URI for accessing all downloads across all UIDs (requires the
     * ACCESS_ALL_DOWNLOADS permission).
     */
    public Uri getAllDownloadsContentUri() {
        return allDownloadsContentUri;
    }

    /**
     * The content:// URI to access downloads owned by the caller's UID.
     */
    public Uri getBatchContentUri() {
        return batchContentUri;
    }

    /**
     * The content:// URI to access downloads owned by the caller's UID.
     */
    public Uri getContentUri() {
        return contentUri;
    }
}
