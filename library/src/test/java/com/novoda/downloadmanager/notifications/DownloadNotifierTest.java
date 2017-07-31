package com.novoda.downloadmanager.notifications;

import android.content.Context;

import com.novoda.downloadmanager.lib.DownloadBatch;
import com.novoda.downloadmanager.lib.NotificationCreatedListener;

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
    @Mock
    private NotificationCreatedListener mockListener;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void whenRemovingStaleNotificationsThenItDoesNotCrash() {
        Collection<DownloadBatch> batches = new ArrayList<>();

        DownloadBatch batchQueuedForWifi = getQueuedForWifiDownloadBatch();
        DownloadBatch batchRunning = getRunningDownloadBatch();

        batches.add(batchQueuedForWifi);
        batches.add(batchRunning);

        Collection<DownloadBatch> updatedBatches = new ArrayList<>();
        DownloadBatch batchQueuedForWifiUpdated = getQueuedForWifiDownloadBatch();
        updatedBatches.add(batchQueuedForWifiUpdated);

        DownloadNotifier downloadNotifier = new SynchronisedDownloadNotifier(mockContext, mockNotificationDisplayer);
        downloadNotifier.updateWith(batches, mockListener);
        downloadNotifier.updateWith(updatedBatches, mockListener);
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
