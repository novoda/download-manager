package com.novoda.downloadmanager.notifications;

import android.content.Context;

import com.novoda.downloadmanager.lib.DownloadBatch;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DownloadNotifierTest {

    @Mock
    private NotificationDisplayer mockNotificationDisplayer;
    @Mock
    private Context mockContext;
    private DownloadNotifier downloadNotifier;

    @Before
    public void setUp() {
        initMocks(this);
        downloadNotifier = new SynchronisedDownloadNotifier(mockContext, mockNotificationDisplayer);
    }

    @Test
    public void whenRemovingStaleNotificationsThenItDoesNotCrash() {
        Collection<DownloadBatch> batches = createDownloadBatches();

        Collection<DownloadBatch> updatedBatches = new ArrayList<>();
        DownloadBatch batchQueuedForWifiUpdated = getQueuedForWifiDownloadBatch();
        updatedBatches.add(batchQueuedForWifiUpdated);

        downloadNotifier.updateWith(batches, notificationNotifier);
        downloadNotifier.updateWith(updatedBatches, notificationNotifier);
    }

    private Collection<DownloadBatch> createDownloadBatches() {
        Collection<DownloadBatch> batches = new ArrayList<>();

        DownloadBatch batchRunning = getRunningDownloadBatch();

        batches.add(getQueuedForWifiDownloadBatch());
        batches.add(getQueuedForWifiDownloadBatch());
        batches.add(getQueuedForWifiDownloadBatch());
        batches.add(getQueuedForWifiDownloadBatch());
        batches.add(getQueuedForWifiDownloadBatch());
        batches.add(getQueuedForWifiDownloadBatch());

        batches.add(batchRunning);

        return batches;
    }

    private DownloadBatch getQueuedForWifiDownloadBatch() {
        DownloadBatch downloadBatch = mock(DownloadBatch.class);
        when(downloadBatch.shouldShowActiveItem()).thenReturn(true);
        when(downloadBatch.isQueuedForWifi()).thenReturn(true);
        return downloadBatch;
    }

    private DownloadBatch getRunningDownloadBatch() {
        DownloadBatch downloadBatch = mock(DownloadBatch.class);
        when(downloadBatch.shouldShowActiveItem()).thenReturn(true);
        when(downloadBatch.isRunning()).thenReturn(true);
        return downloadBatch;
    }
}
