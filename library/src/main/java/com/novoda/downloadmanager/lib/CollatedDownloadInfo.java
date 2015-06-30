package com.novoda.downloadmanager.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CollatedDownloadInfo {

    private final long totalSize;

    static CollatedDownloadInfo collateInfo(Map<Long, DownloadInfo> mDownloads, DownloadInfo info) {
        List<DownloadInfo> downloadInfosForBatch = new ArrayList<>();
        downloadInfosForBatch.add(info);
        for (Map.Entry<Long, DownloadInfo> entry : mDownloads.entrySet()) {
            DownloadInfo otherInfo = entry.getValue();
            if (info.mId == otherInfo.mId) {
                downloadInfosForBatch.add(otherInfo);
            }
        }
        return new CollatedDownloadInfo(sumTotalSizeFrom(downloadInfosForBatch));
    }

    private static long sumTotalSizeFrom(List<DownloadInfo> downloadInfos) {
        long size = 0L;
        int downloadCount = downloadInfos.size();
        for (int i = 0; i < downloadCount; i++) {
            DownloadInfo downloadInfo = downloadInfos.get(i);
            size += downloadInfo.mTotalBytes;
        }
        return size;
    }

    public CollatedDownloadInfo(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getTotalSizeInBytes() {
        return totalSize;
    }
}
