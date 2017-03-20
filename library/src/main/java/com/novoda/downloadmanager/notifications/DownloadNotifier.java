package com.novoda.downloadmanager.notifications;

import com.novoda.downloadmanager.lib.DownloadBatch;

import java.util.Collection;

public interface DownloadNotifier {

    void cancelAll();

    void notifyDownloadSpeed(long id, long bytesPerSecond);

    void updateWith(Collection<DownloadBatch> batches);
}
