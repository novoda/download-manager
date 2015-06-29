package com.novoda.downloadmanager.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CollatedDownloadInfo {
    private final List<DownloadInfo> downloadInfos;

    static CollatedDownloadInfo collateInfo(Map<Long, DownloadInfo> mDownloads, DownloadInfo info) {
        CollatedDownloadInfo collatedDownloadInfo = new CollatedDownloadInfo(new ArrayList<DownloadInfo>());
        collatedDownloadInfo.add(info);
        for (Map.Entry<Long, DownloadInfo> entry : mDownloads.entrySet()) {
            DownloadInfo otherInfo = entry.getValue();
            if (info.mId == otherInfo.mId) {
                collatedDownloadInfo.add(otherInfo);
            }
        }
        return collatedDownloadInfo;
    }

    private CollatedDownloadInfo(List<DownloadInfo> downloadInfos) {
        this.downloadInfos = downloadInfos;
    }

    public void add(DownloadInfo info) {
        downloadInfos.add(info);
    }

    public long getTotalSizeInBytes() {
        long size = 0L;
        int downloadCount = downloadInfos.size();
        for (int i = 0; i < downloadCount; i++) {
            DownloadInfo downloadInfo = downloadInfos.get(i);
            size += downloadInfo.mTotalBytes;
        }
        return size;
    }
}
