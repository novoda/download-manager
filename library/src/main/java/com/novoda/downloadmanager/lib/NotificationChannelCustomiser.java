package com.novoda.downloadmanager.lib;

public interface NotificationChannelCustomiser {

    String getId();

    String getName();

    String description();

    Importance importance();

    enum Importance {
        NONE,
        LOW,
        MEDIUM,
        HIGH
    }
}
