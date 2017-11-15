package com.novoda.downloadmanager;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

final class SchedulerFactory {

    private SchedulerFactory() {
        // Uses static methods.
    }

    static Scheduler createFixedRateTimerScheduler(long frequencyInMillis) {
        return new FixedRateTimerScheduler(new Timer(), frequencyInMillis, new HashMap<Scheduler.Action, TimerTask>());
    }

}
