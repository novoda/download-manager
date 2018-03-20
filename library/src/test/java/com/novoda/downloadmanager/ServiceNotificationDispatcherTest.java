package com.novoda.downloadmanager;

import android.app.Notification;
import android.support.v4.app.NotificationManagerCompat;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ServiceNotificationDispatcherTest {

    private static final String NOTIFICATION_TAG = "download-manager";

    private static final DownloadBatchStatus DOWNLOAD_BATCH_STATUS = anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.QUEUED).build();
    private static final DownloadBatchStatus ANOTHER_DOWNLOAD_BATCH_STATUS = anInternalDownloadsBatchStatus().withStatus(DownloadBatchStatus.Status.QUEUED).build();

    private final Object lock = spy(new Object());
    private final NotificationCreator<DownloadBatchStatus> notificationCreator = mock(NotificationCreator.class);
    private final NotificationManagerCompat notificationManager = mock(NotificationManagerCompat.class);
    private final DownloadManagerService downloadService = mock(DownloadManagerService.class);

    private ServiceNotificationDispatcher<DownloadBatchStatus> notificationDispatcher;

    @Before
    public void setUp() {
        given(notificationCreator.createNotification(any(DownloadBatchStatus.class))).willReturn(createNotificationInfo(SINGLE_PERSISTENT_NOTIFICATION, 100));

        notificationDispatcher = new ServiceNotificationDispatcher<>(lock, notificationCreator, notificationManager);
        notificationDispatcher.setService(downloadService);
    }

    @Test
    public void showsSinglePersistentNotification() {
        NotificationInformation notificationInfo = createNotificationInfo(SINGLE_PERSISTENT_NOTIFICATION, 100);
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(notificationInfo);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        verify(downloadService).start(notificationInfo.getId(), notificationInfo.getNotification());
    }

    @Test
    public void stacksDismissibleNotification() {
        NotificationInformation notificationInfo = createNotificationInfo(STACK_NOTIFICATION_DISMISSIBLE, 100);
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(notificationInfo);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        verify(downloadService, never()).stop(true);
        verify(notificationManager).notify(NOTIFICATION_TAG, notificationInfo.getId(), notificationInfo.getNotification());
    }

    @Test
    public void stopsService_andStacksDismissibleNotification_ifNotificationWasPersistent() {
        NotificationInformation persistentNotificationInfo = createNotificationInfo(SINGLE_PERSISTENT_NOTIFICATION, 100);
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(persistentNotificationInfo);
        NotificationInformation stackNotificationInfo = createNotificationInfo(STACK_NOTIFICATION_DISMISSIBLE, 100);
        given(notificationCreator.createNotification(ANOTHER_DOWNLOAD_BATCH_STATUS)).willReturn(stackNotificationInfo);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);
        notificationDispatcher.updateNotification(ANOTHER_DOWNLOAD_BATCH_STATUS);

        InOrder inOrder = inOrder(downloadService, notificationManager);
        inOrder.verify(downloadService).stop(true);
        inOrder.verify(notificationManager).notify(NOTIFICATION_TAG, stackNotificationInfo.getId(), stackNotificationInfo.getNotification());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void stacksNonDismissibleNotification() {
        NotificationInformation notificationInfo = createNotificationInfo(STACK_NOTIFICATION_NOT_DISMISSIBLE, 100);
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(notificationInfo);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        verify(downloadService, never()).stop(true);
        verify(notificationManager).notify(NOTIFICATION_TAG, notificationInfo.getId(), notificationInfo.getNotification());
    }

    @Test
    public void stopsService_andStacksNonDismissibleNotification_ifNotificationWasPersistent() {
        NotificationInformation persistentNotificationInfo = createNotificationInfo(SINGLE_PERSISTENT_NOTIFICATION, 100);
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(persistentNotificationInfo);
        NotificationInformation stackNotificationInfo = createNotificationInfo(STACK_NOTIFICATION_NOT_DISMISSIBLE, 100);
        given(notificationCreator.createNotification(ANOTHER_DOWNLOAD_BATCH_STATUS)).willReturn(stackNotificationInfo);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);
        notificationDispatcher.updateNotification(ANOTHER_DOWNLOAD_BATCH_STATUS);

        InOrder inOrder = inOrder(downloadService, notificationManager);
        inOrder.verify(downloadService).stop(true);
        inOrder.verify(notificationManager).notify(NOTIFICATION_TAG, stackNotificationInfo.getId(), stackNotificationInfo.getNotification());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void addsOngoingEvent_whenStackingNonDismissibleNotification() {
        NotificationInformation notificationInfo = createNotificationInfo(STACK_NOTIFICATION_NOT_DISMISSIBLE, 100);
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(notificationInfo);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        assertThat(notificationInfo.getNotification().flags).isEqualTo(Notification.FLAG_ONGOING_EVENT);
    }

    @Test
    public void dismissesStackedNotification_whenUpdatingNotification() {
        NotificationInformation notificationInfo = createNotificationInfo(SINGLE_PERSISTENT_NOTIFICATION, 100);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        verify(notificationManager).cancel(NOTIFICATION_TAG, notificationInfo.getId());
    }

    @Test
    public void doesNotStopService_whenNotificationIsHidden_andNotificationWasNotPersistent() {
        NotificationInformation persistentNotificationInfo = createNotificationInfo(SINGLE_PERSISTENT_NOTIFICATION, 0);
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(persistentNotificationInfo);
        NotificationInformation hiddenNotificationInfoWithDifferentId = createNotificationInfo(HIDDEN_NOTIFICATION, 100);
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(hiddenNotificationInfoWithDifferentId);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);

        verify(downloadService, never()).stop(true);
    }

    @Test
    public void stopsService_whenNotificationIsHidden_andNotificationWasPersistent() {
        NotificationInformation persistentNotificationInfo = createNotificationInfo(SINGLE_PERSISTENT_NOTIFICATION, 100);
        given(notificationCreator.createNotification(DOWNLOAD_BATCH_STATUS)).willReturn(persistentNotificationInfo);
        NotificationInformation hiddenNotificationInfo = createNotificationInfo(HIDDEN_NOTIFICATION, 100);
        given(notificationCreator.createNotification(ANOTHER_DOWNLOAD_BATCH_STATUS)).willReturn(hiddenNotificationInfo);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);
        notificationDispatcher.updateNotification(ANOTHER_DOWNLOAD_BATCH_STATUS);

        verify(downloadService).stop(true);
    }

    @Test(timeout = 500)
    public void waitsForServiceToExist_whenUpdatingNotification() {
        notificationDispatcher.setService(downloadService);

        notificationDispatcher.updateNotification(DOWNLOAD_BATCH_STATUS);
    }

    private NotificationInformation createNotificationInfo(NotificationCustomizer.NotificationDisplayState displayState, int id) {
        return notificationInformation()
                .withNotificationDisplayState(displayState)
                .withId(id)
                .build();
    }

}
