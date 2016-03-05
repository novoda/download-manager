package com.novoda.downloadmanager.lib;

import android.content.ContentUris;
import android.net.Uri;

public class DownloadsUriProvider { // Why is this a singleton if all fields are final?

    private final Uri downloadsByBatchUri;
    private final Uri allDownloadsUri;
    private final Uri batchesUri;
    private final Uri contentUri;
    private final Uri downloadsWithoutProgressUri;
    private final Uri batchesWithoutProgressUri;

    public static DownloadsUriProvider getInstance() {
        return LazyInitialisationHelper.INSTANCE;
    }

    private static class LazyInitialisationHelper {
        private static final DownloadsUriProvider INSTANCE = newInstance();
    }

    private static DownloadsUriProvider newInstance() {
        String authority = "content://" + DownloadProvider.AUTHORITY;

        Uri downloadsByBatchUri = Uri.parse(authority + "/downloads_by_batch");
        Uri allDownloadsUri = Uri.parse(authority + "/all_downloads");
        Uri batchesUri = Uri.parse(authority + "/batches");
        Uri contentUri = Uri.parse(authority + "/my_downloads");
        Uri downloadsByStatusUri = Uri.parse(authority + "/downloads_without_progress");
        Uri batchesByStatusUri = Uri.parse(authority + "/batches_without_progress");

        return new DownloadsUriProvider(
                downloadsByBatchUri,
                allDownloadsUri,
                batchesUri,
                contentUri,
                downloadsByStatusUri,
                batchesByStatusUri
        );
    }

    DownloadsUriProvider(
            Uri downloadsByBatchUri,
            Uri allDownloadsUri,
            Uri batchesUri,
            Uri contentUri,
            Uri downloadsWithoutProgressUri, Uri batchesWithoutProgressUri) {
        this.downloadsByBatchUri = downloadsByBatchUri;
        this.allDownloadsUri = allDownloadsUri;
        this.batchesUri = batchesUri;
        this.contentUri = contentUri;
        this.downloadsWithoutProgressUri = downloadsWithoutProgressUri;
        this.batchesWithoutProgressUri = batchesWithoutProgressUri;
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
    public Uri getAllDownloadsUri() {
        return allDownloadsUri;
    }

    /**
     * The content:// URI to access downloads owned by the caller's UID.
     */
    public Uri getBatchesUri() {
        return batchesUri;
    }

    /**
     * The content:// URI to access one download owned by the caller's UID.
     */
    public Uri getSingleBatchUri(long batchId) {
        return ContentUris.withAppendedId(batchesUri, batchId);
    }

    /**
     * The content:// URI to access downloads owned by the caller's UID.
     */
    public Uri getContentUri() {
        return contentUri;
    }

    /**
     * The content:// URI to access downloads without progress updates.
     */
    public Uri getDownloadsWithoutProgressUri() {
        return downloadsWithoutProgressUri;
    }
    /**
     * The content:// URI to access batches without progress updates.
     */
    public Uri getBatchesWithoutProgressUri() {
        return batchesWithoutProgressUri;
    }
}
