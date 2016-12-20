package com.novoda.downloadmanager.download.task;

class State {
    public boolean gotData;
    public long currentBytes;
    public long speedSampleStart;
    public long speedSampleBytes;
    public long speed;
    public long bytesNotified;
    public long timeLastNotification;
}
