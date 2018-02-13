package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class NotificationDispatcherTest {

    private static final DownloadBatchStatus QUEUED_BATCH_STATUS = InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.QUEUED).build();
    private static final DownloadBatchStatus DOWNLOADING_BATCH_STATUS = InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.DOWNLOADING).build();
    private static final DownloadBatchStatus PAUSED_BATCH_STATUS = InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.PAUSED).build();
    private static final DownloadBatchStatus ERROR_BATCH_STATUS = InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.ERROR).build();
    private static final DownloadBatchStatus DELETED_BATCH_STATUS = InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.DELETED).build();
    private static final DownloadBatchStatus DOWNLOADED_BATCH_STATUS = InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.DOWNLOADED).build();

    private final NotificationInformation notificationInformation = mock(NotificationInformation.class);
    private final Object lock = spy(new Object());
    private final NotificationCreator<DownloadBatchStatus> notificationCreator = mock(NotificationCreator.class);
    private final DownloadsNotificationSeenPersistence persistence = mock(DownloadsNotificationSeenPersistence.class);
    private final DownloadService downloadService = mock(DownloadService.class);

    private NotificationDispatcher notificationDispatcher;

    @Before
    public void setUp() {
        Log.setShowLogs(false);
        notificationDispatcher = new NotificationDispatcher(lock, notificationCreator, persistence);
        notificationDispatcher.setDownloadService(downloadService);

        given(notificationCreator.createNotification(any(DownloadBatchStatus.class))).willReturn(notificationInformation);
    }

    @Test
    public void stacksNotification_whenStatusIsDownloaded() {
        notificationDispatcher.updateNotification(DOWNLOADED_BATCH_STATUS);

        verify(downloadService).stackNotification(notificationInformation);
    }

    @Test
    public void stacksNonDismissibleNotification_whenStatusIsPaused() {
        notificationDispatcher.updateNotification(PAUSED_BATCH_STATUS);

        verify(downloadService).stackNotificationNotDismissible(notificationInformation);
    }

    @Test
    public void stacksNotification_whenStatusIsDeleted() {
        notificationDispatcher.updateNotification(DELETED_BATCH_STATUS);

        verify(downloadService).stackNotification(notificationInformation);
    }

    @Test
    public void stacksNotification_whenStatusIsError() {
        notificationDispatcher.updateNotification(ERROR_BATCH_STATUS);

        verify(downloadService).stackNotification(notificationInformation);
    }

    @Test
    public void updatesNotification_whenStatusIsDownloading() {
        notificationDispatcher.updateNotification(DOWNLOADING_BATCH_STATUS);

        verify(downloadService).updateNotification(notificationInformation);
    }

    @Test
    public void dismissesStackedNotification_whenUpdatingNotification() {
        List<DownloadBatchStatus> allDownloadBatchStatuses = Arrays.asList(QUEUED_BATCH_STATUS, DOWNLOADING_BATCH_STATUS, PAUSED_BATCH_STATUS, ERROR_BATCH_STATUS, DELETED_BATCH_STATUS, DOWNLOADED_BATCH_STATUS);

        for (DownloadBatchStatus downloadBatchStatus : allDownloadBatchStatuses) {
            notificationDispatcher.updateNotification(downloadBatchStatus);
            verify(downloadService).dismissStackedNotification(notificationInformation);
            reset(downloadService);
        }
    }

    @Test
    public void doesNotUpdateNotificationSeen_whenDownloadBatchStatusIsAnythingButDownloaded() {
        List<DownloadBatchStatus> allStatusesMinusDownloaded = Arrays.asList(QUEUED_BATCH_STATUS, DOWNLOADING_BATCH_STATUS, PAUSED_BATCH_STATUS, ERROR_BATCH_STATUS, DELETED_BATCH_STATUS);

        for (DownloadBatchStatus downloadBatchStatus : allStatusesMinusDownloaded) {
            notificationDispatcher.updateNotification(downloadBatchStatus);
        }

        verifyZeroInteractions(persistence);
    }

    @Test
    public void updatesNotificationSeen_whenStatusIsDownloaded() {
        notificationDispatcher.updateNotification(DOWNLOADED_BATCH_STATUS);

        verify(persistence).updateNotificationSeenAsync(DOWNLOADED_BATCH_STATUS.getDownloadBatchId(), true);
    }

    @Test
    public void doesNotUpdateNotifications_whenNotificationHasBeenSeen() {
        InternalDownloadBatchStatus notificationSeenStatus = InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus().withNotificationSeen(true).build();

        notificationDispatcher.updateNotification(notificationSeenStatus);

        InOrder inOrder = Mockito.inOrder(downloadService);
        inOrder.verify(downloadService).dismissStackedNotification(any(NotificationInformation.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test(timeout = 500)
    public void waitsForServiceToExist_whenUpdatingNotification() {
        notificationDispatcher.setDownloadService(downloadService);

        notificationDispatcher.updateNotification(DOWNLOADING_BATCH_STATUS);
    }

}
