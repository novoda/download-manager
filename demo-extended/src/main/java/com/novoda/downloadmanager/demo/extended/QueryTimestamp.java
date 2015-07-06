package com.novoda.downloadmanager.demo.extended;

import java.util.concurrent.TimeUnit;

public class QueryTimestamp {
    private static final long MAX_UPDATE_FREQUENCY_MILLIS = TimeUnit.SECONDS.toMillis(1);

    private long timestamp;

    public void setJustUpdated() {
        timestamp = now();
    }

    private long now() {
        return System.currentTimeMillis();
    }

    public boolean updatedRecently() {
        long timeSinceLastUpdate = now() - timestamp;
        return timeSinceLastUpdate < MAX_UPDATE_FREQUENCY_MILLIS;
    }
}
