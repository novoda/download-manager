package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.Timer;

final class SchedulerFactory {

    private SchedulerFactory() {
        // Uses static methods.
    }

    static Scheduler createFixedRateTimerScheduler(long frequencyInMillis) {
        return new FixedRateTimerScheduler(new Timer(), frequencyInMillis, new ArrayList<Scheduler.Action>());
    }

}
