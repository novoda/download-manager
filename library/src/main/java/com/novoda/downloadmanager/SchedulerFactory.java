package com.novoda.downloadmanager;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

final class SchedulerFactory {

    private SchedulerFactory() {
        // Uses static methods.
    }

    static ActionScheduler createFixedRateTimerScheduler(long frequencyInMillis) {
        return new FixedRateTimerActionScheduler(new Timer(), frequencyInMillis, new HashMap<ActionScheduler.Action, TimerTask>());
    }

}
