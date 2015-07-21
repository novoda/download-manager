package com.novoda.downloadmanager.lib;

public interface DataWriter {

    DownloadThread.State write(DownloadThread.State state, byte[] buffer, int count) throws StopRequestException;

}
