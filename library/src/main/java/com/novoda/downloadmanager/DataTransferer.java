package com.novoda.downloadmanager;

import java.io.InputStream;

interface DataTransferer {
    State transferData(State state, InputStream in);
}
