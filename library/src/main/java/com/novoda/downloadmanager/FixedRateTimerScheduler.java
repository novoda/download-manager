package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class FixedRateTimerScheduler implements Scheduler {

    private static final int DELAY_IN_MILLIS = 0;
    private final Timer timer;
    private final long frequency;

    private List<Action> actions = new ArrayList<>();

    static FixedRateTimerScheduler withFrequency(long frequency) {
        return new FixedRateTimerScheduler(new Timer(), frequency);
    }

    private FixedRateTimerScheduler(Timer timer, long frequency) {
        this.timer = timer;
        this.frequency = frequency;
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
        }, DELAY_IN_MILLIS, frequency);
        actions.add(action);
    }

    @Override
    public void cancel(Action action) {
        // Timer can't cancel per action.
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
