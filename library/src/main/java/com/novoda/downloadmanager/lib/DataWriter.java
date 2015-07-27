package com.novoda.downloadmanager.lib;

interface DataWriter {

    DownloadTask.State write(DownloadTask.State state, byte[] buffer, int count) throws StopRequestException;

}
