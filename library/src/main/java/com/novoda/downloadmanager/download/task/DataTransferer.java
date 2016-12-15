package com.novoda.downloadmanager.download.task;

import java.io.InputStream;

interface DataTransferer {
    State transferData(State state, InputStream in);
}