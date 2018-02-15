package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

import org.junit.Before;
import org.junit.Test;

import static com.novoda.downloadmanager.InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class DownloadBatchStatusNotificationDispatcherTest {

    private final ServiceNotificationDispatcher<DownloadBatchStatus> notificationDispatcher = mock(ServiceNotificationDispatcher.class);
    private final DownloadsNotificationSeenPersistence persistence = mock(DownloadsNotificationSeenPersistence.class);

    private DownloadBatchStatusNotificationDispatcher downloadBatchStatusNotificationDispatcher;

    @Before
    public void setUp() {
        Log.setShowLogs(false);
        downloadBatchStatusNotificationDispatcher = new DownloadBatchStatusNotificationDispatcher(persistence, notificationDispatcher);
    }

    @Test
    public void updatesNotificationSeen_whenStatusIsDownloaded() {
        InternalDownloadBatchStatus downloadedBatchStatus = anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.DOWNLOADED).build();

        downloadBatchStatusNotificationDispatcher.updateNotification(downloadedBatchStatus);

        verify(persistence).updateNotificationSeenAsync(downloadedBatchStatus.getDownloadBatchId(), true);
    }

    @Test
    public void doesNothing_whenNotificationHasBeenSeen() {
        InternalDownloadBatchStatus notificationSeenStatus = anInternalDownloadsBatchStatus().withNotificationSeen(true).build();

        downloadBatchStatusNotificationDispatcher.updateNotification(notificationSeenStatus);

        verifyZeroInteractions(notificationDispatcher, persistence);
    }

    @Test
    public void updatesNotification_whenNotificationHasNotBeenSeen() {
        InternalDownloadBatchStatus notificationNotSeenStatus = anInternalDownloadsBatchStatus().withNotificationSeen(false).build();

        downloadBatchStatusNotificationDispatcher.updateNotification(notificationNotSeenStatus);

        verify(notificationDispatcher).updateNotification(notificationNotSeenStatus);
    }

    @Test
    public void setsDownloadServiceOnNotificationDispatcher() {
        DownloadService downloadService = mock(LiteDownloadService.class);

        downloadBatchStatusNotificationDispatcher.setDownloadService(downloadService);

        verify(notificationDispatcher).setService(downloadService);
    }

}
