package com.novoda.downloadmanager;

import java.util.HashMap;
import java.util.Timer;

final class SchedulerFactory {

    private SchedulerFactory() {
        // Uses static methods.
    }

    static ActionScheduler createFixedRateTimerScheduler(long frequencyInMillis) {
        return new FixedRateTimerActionScheduler(new Timer(), frequencyInMillis, new HashMap<>());
    }

}
