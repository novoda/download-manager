package com.novoda.downloadmanager.task;

import java.io.InputStream;

interface DataTransferer {
    State transferData(State state, InputStream in);
}