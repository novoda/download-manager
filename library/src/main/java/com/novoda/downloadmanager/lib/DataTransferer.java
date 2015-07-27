package com.novoda.downloadmanager.lib;

import java.io.InputStream;

interface DataTransferer {
    DownloadTask.State transferData(DownloadTask.State state, InputStream in) throws StopRequestException;
}
