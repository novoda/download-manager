package com.novoda.downloadmanager;

import android.app.Notification;

import static com.novoda.downloadmanager.NotificationCustomizer.NotificationStackState.SINGLE_PERSISTENT_NOTIFICATION;
import static org.mockito.Mockito.mock;

class NotificationInformationFixtures {

    private int id = 0;
    private Notification notification = mock(Notification.class);
    private NotificationCustomizer.NotificationStackState notificationStackState = SINGLE_PERSISTENT_NOTIFICATION;

    static NotificationInformationFixtures notificationInformation() {
        return new NotificationInformationFixtures();
    }

    NotificationInformationFixtures withId(int id) {
        this.id = id;
        return this;
    }

    NotificationInformationFixtures withNotification(Notification notification) {
        this.notification = notification;
        return this;
    }

    NotificationInformationFixtures withNotificationStackState(NotificationCustomizer.NotificationStackState notificationStackState) {
        this.notificationStackState = notificationStackState;
        return this;
    }

    NotificationInformation build() {
        return new NotificationInformation() {
            @Override
            public int getId() {
                return id;
            }

            @Override
            public Notification getNotification() {
                return notification;
            }

            @Override
            public NotificationCustomizer.NotificationStackState notificationStackState() {
                return notificationStackState;
            }
        };
    }
}
