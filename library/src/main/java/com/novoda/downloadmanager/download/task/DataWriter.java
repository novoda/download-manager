package com.novoda.downloadmanager.download.task;

interface DataWriter {

    State write(State state, byte[] buffer, int count);

}