package com.novoda.downloadmanager;

interface DataWriter {

    State write(State state, byte[] buffer, int count);

}
