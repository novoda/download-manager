package com.novoda.downloadmanager.lib;

import java.util.ArrayList;
import java.util.Map;

public final class CollatedDownloadInfo {
    private final ArrayList<DownloadInfo> downloadInfos;

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

    private CollatedDownloadInfo(ArrayList<DownloadInfo> downloadInfos) {
        this.downloadInfos = downloadInfos;
    }

    public void add(DownloadInfo info) {
        downloadInfos.add(info);
    }
}
