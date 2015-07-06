package com.novoda.downloadmanager.lib;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.MockitoAnnotations.initMocks;

public class DownloadNotifierTest {

    @Mock
    private NotificationDisplayer mockNotificationDisplayer;
    @Mock
    private Context mockContext;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void whenRemovingStaleNotificationsThenItDoesNotCrash() {
        Collection<DownloadBatch> batches = new ArrayList<>();

        DownloadBatch batchQueuedForWifi = getDownloadBatchWith(Downloads.Impl.STATUS_QUEUED_FOR_WIFI);
        DownloadBatch batchRunning = getDownloadBatchWith(Downloads.Impl.STATUS_RUNNING);

        batches.add(batchQueuedForWifi);
        batches.add(batchRunning);

        Collection<DownloadBatch> updatedBatches = new ArrayList<>();
        DownloadBatch batchQueuedForWifiUpdated = getDownloadBatchWith(Downloads.Impl.STATUS_QUEUED_FOR_WIFI);
        updatedBatches.add(batchQueuedForWifiUpdated);

        DownloadNotifier downloadNotifier = new DownloadNotifier(mockContext, mockNotificationDisplayer);
        downloadNotifier.updateWith(batches);
        downloadNotifier.updateWith(updatedBatches);
    }

    private DownloadBatch getDownloadBatchWith(int status) {
        return new DownloadBatch(
                1,
                new BatchInfo("", "", "", NotificationVisibility.ACTIVE_OR_COMPLETE),
                new ArrayList<DownloadInfo>(),
                status,
                1,
                1);
    }
}
