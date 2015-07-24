package com.novoda.downloadmanager.lib;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

class Clock {

    public enum Interval {
        ONE_SECOND(TimeUnit.SECONDS.toMillis(1));

        private final long interval;

        Interval(long interval) {
            this.interval = interval;
        }

        public long toMillis() {
            return interval;
        }
    }

    private long lastUpdate;

    public void startInterval() {
        lastUpdate = SystemClock.elapsedRealtime();
    }

    public boolean intervalLessThan(Interval interval) {
        return SystemClock.elapsedRealtime() - lastUpdate < interval.toMillis();
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
