package com.novoda.downloadmanager.lib;

import android.app.Notification;
import android.app.Service;
import android.support.v4.util.SimpleArrayMap;

import com.novoda.downloadmanager.notifications.NotificationTag;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.novoda.downloadmanager.notifications.DownloadNotifier.TYPE_ACTIVE;
import static com.novoda.downloadmanager.notifications.DownloadNotifier.TYPE_FAILED;
import static com.novoda.downloadmanager.notifications.NotificationTagFixtures.aTag;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class NotificationCreatedListenerTest {

    private static final boolean KEEP_NOTIFICATION = false;

    @Mock
    private Service service;
    @Mock
    private Notification activeDownloadNotification;
    @Mock
    private Notification failedDownloadNotification;

    private final NotificationTag activeDownloadTag = aTag().withStatus(TYPE_ACTIVE).build();

    private final NotificationTag failedDownloadTag = aTag().withStatus(TYPE_FAILED).build();

    private NotificationCreatedListener listener;

    @Before
    public void setUp() {
        initMocks(this);
        listener = new NotificationCreatedListener(service);
    }

    @Test
    public void givenActiveDownload_whenNotificationCreated_thenBringsServiceToForeground() {
        SimpleArrayMap<NotificationTag, Notification> taggedNotifications = new SimpleArrayMap<>();
        taggedNotifications.put(activeDownloadTag, activeDownloadNotification);

        listener.onNotificationCreated(taggedNotifications);

        verify(service).startForeground(activeDownloadTag.hashCode(), activeDownloadNotification);
    }

    @Test
    public void givenFailedDownload_whenNotificationCreated_thenPutsServiceInBackground() {
        SimpleArrayMap<NotificationTag, Notification> taggedNotifications = new SimpleArrayMap<>();
        taggedNotifications.put(failedDownloadTag, failedDownloadNotification);

        listener.onNotificationCreated(taggedNotifications);

        verify(service).stopForeground(KEEP_NOTIFICATION);
    }
}
