package com.novoda.downloadmanager;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

class FixedRateTimerActionScheduler implements ActionScheduler {

    private static final long DELAY_IN_MILLIS = 0;

    private final Timer timer;
    private final long frequencyInMillis;
    private final Map<Action, TimerTask> actionTimerTasks;

    FixedRateTimerActionScheduler(Timer timer, long frequencyInMillis, Map<Action, TimerTask> actionTimerTasks) {
        this.timer = timer;
        this.frequencyInMillis = frequencyInMillis;
        this.actionTimerTasks = actionTimerTasks;
    }

    @Override
    public void schedule(final Action action) {
        if (actionTimerTasks.containsKey(action)) {
            return;
        }

        TimerTask taskToExecute = new TimerTask() {
            @Override
            public void run() {
                action.perform();
            }
        };
        timer.scheduleAtFixedRate(taskToExecute, DELAY_IN_MILLIS, frequencyInMillis);
        actionTimerTasks.put(action, taskToExecute);
    }

    @Override
    public void cancel(Action action) {
        if (actionTimerTasks.containsKey(action)) {
            TimerTask timerTask = actionTimerTasks.get(action);
            timerTask.cancel();
            actionTimerTasks.remove(action);
        }
    }

    @Override
    public void cancelAll() {
        timer.cancel();
        actionTimerTasks.clear();
    }

    @Override
    public boolean isScheduled(Action action) {
        return actionTimerTasks.containsKey(action);
    }

}
