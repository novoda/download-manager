package com.novoda.downloadmanager.lib;

import android.app.Notification;
import android.app.Service;
import android.support.v4.util.SimpleArrayMap;

import com.novoda.downloadmanager.notifications.NotificationTag;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.novoda.downloadmanager.notifications.DownloadNotifier.*;
import static com.novoda.downloadmanager.notifications.NotificationTagFixtures.aTag;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class NotificationsCreatedListenerTest {

    private static final boolean KEEP_NOTIFICATION = false;
    private static final NotificationTag ACTIVE_DOWNLOAD_TAG = aTag().withStatus(TYPE_ACTIVE).build();
    private static final NotificationTag FAILED_DOWNLOAD_TAG = aTag().withStatus(TYPE_FAILED).build();
    private static final NotificationTag SUCCESSFUL_DOWNLOAD_TAG = aTag().withStatus(TYPE_SUCCESS).build();
    private static final NotificationTag CANCELLED_DOWNLOAD_TAG = aTag().withStatus(TYPE_CANCELLED).build();
    private static final NotificationTag WAITING_DOWNLOAD_TAG = aTag().withStatus(TYPE_WAITING).build();

    @Mock
    private Service service;

    @Mock
    private Notification activeDownloadNotification;
    @Mock
    private Notification failedDownloadNotification;
    @Mock
    private Notification successfulDownloadNotification;
    @Mock
    private Notification cancelledDownloadNotification;
    @Mock
    private Notification waitingDownloadNotification;

    private NotificationsCreatedListener listener;

    @Before
    public void setUp() {
        initMocks(this);
        listener = new NotificationsCreatedListener(service);
    }

    @Test
    public void givenActiveDownload_whenNotificationCreated_thenBringsServiceToForeground() {
        SimpleArrayMap<NotificationTag, Notification> taggedNotifications = givenActiveDownload();

        listener.onNotificationCreated(taggedNotifications);

        verify(service).startForeground(ACTIVE_DOWNLOAD_TAG.hashCode(), activeDownloadNotification);
    }

    @Test
    public void givenNoActiveDownload_whenNotificationCreated_thenPutsServiceInBackground() {
        SimpleArrayMap<NotificationTag, Notification> taggedNotifications = givenNoActiveDownload();

        listener.onNotificationCreated(taggedNotifications);

        verify(service).stopForeground(KEEP_NOTIFICATION);
    }

    private SimpleArrayMap<NotificationTag, Notification> givenActiveDownload() {
        SimpleArrayMap<NotificationTag, Notification> taggedNotifications = givenNoActiveDownload();
        taggedNotifications.put(ACTIVE_DOWNLOAD_TAG, activeDownloadNotification);
        return taggedNotifications;
    }

    private SimpleArrayMap<NotificationTag, Notification> givenNoActiveDownload() {
        SimpleArrayMap<NotificationTag, Notification> taggedNotifications = new SimpleArrayMap<>();
        taggedNotifications.put(FAILED_DOWNLOAD_TAG, failedDownloadNotification);
        taggedNotifications.put(SUCCESSFUL_DOWNLOAD_TAG, successfulDownloadNotification);
        taggedNotifications.put(CANCELLED_DOWNLOAD_TAG, cancelledDownloadNotification);
        taggedNotifications.put(WAITING_DOWNLOAD_TAG, waitingDownloadNotification);
        return taggedNotifications;
    }
}
