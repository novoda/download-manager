package com.novoda.downloadmanager;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class DownloadFile {

    private final String uri;
    private final long currentSize;
    private final long totalSize;
    private final String localUri;
    private final FileStatus fileStatus;

    public DownloadFile(String uri, long currentSize, long totalSize, String localUri, FileStatus fileStatus) {
        this.uri = uri;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
        this.localUri = localUri;
        this.fileStatus = fileStatus;
    }

    public String getUri() {
        return uri;
    }

    public long currentSize() {
        return currentSize;
    }

    public long totalSize() {
        return totalSize;
    }

    public NetworkRequest getNetworkRequest() {
        return new NetworkRequest();
    }

    public FileStatus getStatus() {
        return fileStatus;
    }

    public String getLocalUri() {
        return localUri;
    }

    public int getPercentage() {
        return Percentage.of(currentSize, totalSize);
    }

    public static class NetworkRequest {

        public List<Pair<String, String>> getHeaders() {
            return new ArrayList<>();
        }

    }

    public enum FileStatus {
        INCOMPLETE,
        COMPLETE,
        FAILED
    }

}
