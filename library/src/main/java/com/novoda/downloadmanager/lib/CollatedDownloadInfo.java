package com.novoda.downloadmanager.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollatedDownloadInfo {

    private final long totalSize;

    static CollatedDownloadInfo collateInfo(Map<Long, DownloadInfo> downloadsMap, DownloadInfo info) {
        List<DownloadInfo> downloadInfosForBatch = new ArrayList<>();
        downloadInfosForBatch.add(info);

        for (DownloadInfo entry : downloadsMap.values()) {
            if (info.id == entry.id) {
                downloadInfosForBatch.add(entry);
            }
        }

        return new CollatedDownloadInfo(sumTotalSizeFrom(downloadInfosForBatch));
    }

    private static long sumTotalSizeFrom(List<DownloadInfo> downloadInfos) {
        long size = 0L;
        int downloadCount = downloadInfos.size();
        for (int i = 0; i < downloadCount; i++) {
            DownloadInfo downloadInfo = downloadInfos.get(i);
            size += downloadInfo.totalBytes;
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
