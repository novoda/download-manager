package com.novoda.downloadmanager.task;

interface DataWriter {

    State write(State state, byte[] buffer, int count);

}