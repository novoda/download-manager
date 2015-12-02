package com.novoda.downloadmanager.domain;

class Percentage {

    public static int of(long current, long total) {
        return (int) (((float) current / (float) total) * 100);
    }

}
