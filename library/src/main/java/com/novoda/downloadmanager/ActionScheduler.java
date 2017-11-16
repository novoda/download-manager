package com.novoda.downloadmanager;

interface ActionScheduler {

    void schedule(Action action);

    void cancel(Action action);

    void cancelAll();

    interface Action {
        void perform();
    }

    boolean isScheduled(Action action);

}
