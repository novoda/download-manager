package com.novoda.downloadmanager.notifications;

public class NotificationTagFixtures {
    private int status;
    private String identifier;

    private NotificationTagFixtures() {
        // use aTag() to get an instance of this class
    }

    public static NotificationTagFixtures aTag() {
        return new NotificationTagFixtures();
    }

    public NotificationTagFixtures withStatus(int status) {
        this.status = status;
        return this;
    }

    public NotificationTagFixtures withIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public NotificationTag build() {
        return new NotificationTag(status, identifier);
    }
}
