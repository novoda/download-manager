package com.novoda.downloadmanager;

class Percentage {

    public static int of(long current, long total) {
        return (int) (((float) current / (float) total) * 100);
    }

}
