package com.novoda.downloadmanager;

import android.app.Notification;
import android.support.v4.app.NotificationManagerCompat;

import com.novoda.notils.logger.simple.Log;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static com.google.common.truth.Truth.assertThat;
import static com.novoda.downloadmanager.InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus;
import static com.novoda.downloadmanager.NotificationCustomizer.NotificationDisplayState.HIDDEN_NOTIFICATION;
import static com.novoda.downloadmanager.NotificationCustomizer.NotificationDisplayState.SINGLE_PERSISTENT_NOTIFICATION;
import static com.novoda.downloadmanager.NotificationCustomizer.NotificationDisplayState.STACK_NOTIFICATION_DISMISSIBLE;
import static com.novoda.downloadmanager.NotificationCustomizer.NotificationDisplayState.STACK_NOTIFICATION_NOT_DISMISSIBLE;
import static com.novoda.downloadmanager.NotificationInformationFixtures.notificationInformation;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ServiceNotificationDispatcherTest {

    private static final String NOTIFICATION_TAG = "download-manager";

    private static final DownloadBatchStatus DOWNLOAD_BATCH_STATUS = anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.QUEUED).build();

    private static final NotificationInformation STACKABLE_DISMISSIBLE_NOTIFICATION_INFORMATION = notificationInformation().withNotificationDisplayState(STACK_NOTIFICATION_DISMISSIBLE).build();
    private static final NotificationInformation STACKABLE_NON_DISMISSIBLE_NOTIFICATION_INFORMATION = notificationInformation().withNotificationDisplayState(STACK_NOTIFICATION_NOT_DISMISSIBLE).build();
    private static final NotificationInformation SINGLE_PERSISTENT_NOTIFICATION_INFORMATION = notificationInformation().withNotificationDisplayState(SINGLE_PERSISTENT_NOTIFICATION).build();
    private static final NotificationInformation HIDDEN_NOTIFICATION_INFORMATION = notificationInformation().withNotificationDisplayState(HIDDEN_NOTIFICATION).build();

    private final Object lock = spy(new Object());
    private final NotificationCreator<DownloadBatchStatus> notificationCreator = mock(NotificationCreator.class);
    private final NotificationManagerCompat notificationManager = mock(NotificationManagerCompat.class);
    private final DownloadManagerService downloadService = mock(DownloadManagerService.class);

    private ServiceNotificationDispatcher<DownloadBatchStatus> notificationDispatcher;

    @Before
    public void setUp() {
        Log.setShowLogs(false);
        given(notificationCreator.createNotification(any(DownloadBatchStatus.class))).willReturn(SINGLE_PERSISTENT_NOTIFICATION_INFORMATION);

        notificationDispatcher = new ServiceNotificationDispatcher<>(lock, notificationCreator, notificationManager);
        notificationDispatcher.setService(downloadService);
    }

    @Test
    public void showsSinglePersistentNotification() {
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(SINGLE_PERSISTENT_NOTIFICATION_INFORMATION);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        verify(downloadService).start(SINGLE_PERSISTENT_NOTIFICATION_INFORMATION.getId(), SINGLE_PERSISTENT_NOTIFICATION_INFORMATION.getNotification());
    }

    @Test
    public void stacksDismissibleNotification() {
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(STACKABLE_DISMISSIBLE_NOTIFICATION_INFORMATION);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        InOrder inOrder = inOrder(downloadService, notificationManager);
        inOrder.verify(downloadService).stop(true);
        inOrder.verify(notificationManager).notify(NOTIFICATION_TAG, STACKABLE_DISMISSIBLE_NOTIFICATION_INFORMATION.getId(), STACKABLE_DISMISSIBLE_NOTIFICATION_INFORMATION.getNotification());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void stacksNonDismissibleNotification() {
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(STACKABLE_NON_DISMISSIBLE_NOTIFICATION_INFORMATION);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        InOrder inOrder = inOrder(downloadService, notificationManager);
        inOrder.verify(downloadService).stop(true);
        inOrder.verify(notificationManager).notify(NOTIFICATION_TAG, STACKABLE_NON_DISMISSIBLE_NOTIFICATION_INFORMATION.getId(), STACKABLE_NON_DISMISSIBLE_NOTIFICATION_INFORMATION.getNotification());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void addsOngoingEvent_whenStackingNonDismissibleNotification() {
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(STACKABLE_NON_DISMISSIBLE_NOTIFICATION_INFORMATION);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        assertThat(STACKABLE_NON_DISMISSIBLE_NOTIFICATION_INFORMATION.getNotification().flags).isEqualTo(Notification.FLAG_ONGOING_EVENT);
    }

    @Test
    public void dismissesStackedNotification_whenUpdatingNotification() {
        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        verify(notificationManager).cancel(NOTIFICATION_TAG, SINGLE_PERSISTENT_NOTIFICATION_INFORMATION.getId());
    }

    @Test
    public void doesNothing_whenNotificationIsHidden() {
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(HIDDEN_NOTIFICATION_INFORMATION);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        InOrder inOrder = inOrder(downloadService, notificationManager);
        inOrder.verify(notificationManager).cancel(NOTIFICATION_TAG, HIDDEN_NOTIFICATION_INFORMATION.getId());
        inOrder.verifyNoMoreInteractions();
    }

    @Test(timeout = 500)
    public void waitsForServiceToExist_whenUpdatingNotification() {
        notificationDispatcher.setService(downloadService);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);
    }

}
