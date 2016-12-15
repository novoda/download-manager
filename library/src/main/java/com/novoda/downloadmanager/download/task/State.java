package com.novoda.downloadmanager.download.task;

class State {
    public boolean gotData;
    public int currentBytes;
    public long speedSampleStart;
    public int speedSampleBytes;
    public long speed;
    public int bytesNotified;
    public long timeLastNotification;
}
