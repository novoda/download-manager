package com.novoda.downloadmanager;

import com.novoda.notils.logger.simple.Log;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static com.novoda.downloadmanager.InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus;
import static com.novoda.downloadmanager.NotificationCustomizer.NotificationStackState.SINGLE_PERSISTENT_NOTIFICATION;
import static com.novoda.downloadmanager.NotificationCustomizer.NotificationStackState.STACK_NOTIFICATION_DISMISSIBLE;
import static com.novoda.downloadmanager.NotificationCustomizer.NotificationStackState.STACK_NOTIFICATION_NOT_DISMISSIBLE;
import static com.novoda.downloadmanager.NotificationInformationFixtures.notificationInformation;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class NotificationDispatcherTest {

    private static final DownloadBatchStatus QUEUED_BATCH_STATUS = anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.QUEUED).build();
    private static final DownloadBatchStatus DOWNLOADING_BATCH_STATUS = anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.DOWNLOADING).build();
    private static final DownloadBatchStatus PAUSED_BATCH_STATUS = anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.PAUSED).build();
    private static final DownloadBatchStatus ERROR_BATCH_STATUS = anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.ERROR).build();
    private static final DownloadBatchStatus DELETED_BATCH_STATUS = anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.DELETED).build();
    private static final DownloadBatchStatus DOWNLOADED_BATCH_STATUS = anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.DOWNLOADED).build();

    private static final NotificationInformation STACKABLE_DISMISSIBLE_NOTIFICATION_INFORMATION = notificationInformation().withNotificationStackState(STACK_NOTIFICATION_DISMISSIBLE).build();
    private static final NotificationInformation STACKABLE_NON_DISMISSIBLE_NOTIFICATION_INFORMATION = notificationInformation().withNotificationStackState(STACK_NOTIFICATION_NOT_DISMISSIBLE).build();
    private static final NotificationInformation SINGLE_PERSISTENT_NOTIFICATION_INFORMATION = notificationInformation().withNotificationStackState(SINGLE_PERSISTENT_NOTIFICATION).build();

    private final Object lock = spy(new Object());
    private final NotificationCreator<DownloadBatchStatus> notificationCreator = mock(NotificationCreator.class);
    private final DownloadService downloadService = mock(DownloadService.class);

    private NotificationDispatcher<DownloadBatchStatus> notificationDispatcher;

    @Before
    public void setUp() {
        Log.setShowLogs(false);
        given(notificationCreator.createNotification(any(DownloadBatchStatus.class))).willReturn(SINGLE_PERSISTENT_NOTIFICATION_INFORMATION);

        notificationDispatcher = new NotificationDispatcher<>(lock, notificationCreator);
        notificationDispatcher.setDownloadService(downloadService);
    }

    @Test
    public void showsSinglePersistentNotification() {
        given(notificationCreator.createNotification(QUEUED_BATCH_STATUS)).willReturn(SINGLE_PERSISTENT_NOTIFICATION_INFORMATION);

        notificationDispatcher.updateNotification(QUEUED_BATCH_STATUS);

        verify(downloadService).updateNotification(SINGLE_PERSISTENT_NOTIFICATION_INFORMATION);
    }

    @Test
    public void stacksNonDismissibleNotification() {
        given(notificationCreator.createNotification(QUEUED_BATCH_STATUS)).willReturn(STACKABLE_NON_DISMISSIBLE_NOTIFICATION_INFORMATION);

        notificationDispatcher.updateNotification(QUEUED_BATCH_STATUS);

        verify(downloadService).stackNotificationNotDismissible(STACKABLE_NON_DISMISSIBLE_NOTIFICATION_INFORMATION);
    }

    @Test
    public void stacksDismissibleNotification() {
        given(notificationCreator.createNotification(QUEUED_BATCH_STATUS)).willReturn(STACKABLE_DISMISSIBLE_NOTIFICATION_INFORMATION);

        notificationDispatcher.updateNotification(QUEUED_BATCH_STATUS);

        verify(downloadService).stackNotification(STACKABLE_DISMISSIBLE_NOTIFICATION_INFORMATION);
    }

    @Test
    public void dismissesStackedNotification_whenUpdatingNotification() {
        List<DownloadBatchStatus> allDownloadBatchStatuses = Arrays.asList(QUEUED_BATCH_STATUS, DOWNLOADING_BATCH_STATUS, PAUSED_BATCH_STATUS, ERROR_BATCH_STATUS, DELETED_BATCH_STATUS, DOWNLOADED_BATCH_STATUS);

        for (DownloadBatchStatus downloadBatchStatus : allDownloadBatchStatuses) {
            notificationDispatcher.updateNotification(downloadBatchStatus);
            verify(downloadService).dismissStackedNotification(SINGLE_PERSISTENT_NOTIFICATION_INFORMATION);
            reset(downloadService);
        }
    }

    @Test(timeout = 500)
    public void waitsForServiceToExist_whenUpdatingNotification() {
        notificationDispatcher.setDownloadService(downloadService);

        notificationDispatcher.updateNotification(DOWNLOADING_BATCH_STATUS);
    }

}
