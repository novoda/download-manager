package com.novoda.downloadmanager.notifications;

import android.content.Context;

import com.novoda.downloadmanager.lib.BatchInfo;
import com.novoda.downloadmanager.lib.DownloadBatch;
import com.novoda.downloadmanager.lib.DownloadStatus;
import com.novoda.downloadmanager.lib.FileDownloadInfo;
import com.novoda.downloadmanager.notifications.DownloadNotifier;
import com.novoda.downloadmanager.notifications.NotificationDisplayer;
import com.novoda.downloadmanager.notifications.NotificationVisibility;

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

        DownloadBatch batchQueuedForWifi = getDownloadBatchWith(196); // Queued for Wifi
        DownloadBatch batchRunning = getDownloadBatchWith(192); // Running

        batches.add(batchQueuedForWifi);
        batches.add(batchRunning);

        Collection<DownloadBatch> updatedBatches = new ArrayList<>();
        DownloadBatch batchQueuedForWifiUpdated = getDownloadBatchWith(192); // Queued for Wifi
        updatedBatches.add(batchQueuedForWifiUpdated);

        DownloadNotifier downloadNotifier = new SynchronisedDownloadNotifier(mockContext, mockNotificationDisplayer);
        downloadNotifier.updateWith(batches);
        downloadNotifier.updateWith(updatedBatches);
    }

    private DownloadBatch getDownloadBatchWith(int status) {
        return new DownloadBatch(
                1,
                new BatchInfo("", "", "", NotificationVisibility.ACTIVE_OR_COMPLETE, ""),
                new ArrayList<FileDownloadInfo>(),
                status,
                1,
                1);
    }
}
