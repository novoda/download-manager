package com.novoda.downloadmanager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class FixedRateTimerScheduler implements Scheduler {

    private static final long DELAY_IN_MILLIS = 0;

    private final Timer timer;
    private final long frequencyInMillis;
    private final List<Action> actions;

    FixedRateTimerScheduler(Timer timer, long frequencyInMillis, List<Action> actions) {
        this.timer = timer;
        this.frequencyInMillis = frequencyInMillis;
        this.actions = actions;
    }

    @Override
    public void schedule(final Action action) {
        if (actions.contains(action)) {
            return;
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                action.perform();
            }
        }, DELAY_IN_MILLIS, frequencyInMillis);
        actions.add(action);
    }

    @Override
    public void cancel(Action action) {
        throw new UnsupportedOperationException("Timer cannot cancel per action. Call `cancelAll()` instead.");
    }

    @Override
    public void cancelAll() {
        timer.cancel();
        actions.clear();
    }

    @Override
    public boolean isScheduled(Action action) {
        return actions.contains(action);
    }

}
