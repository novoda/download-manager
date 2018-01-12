package com.novoda.downloadmanager;

import android.support.annotation.Nullable;

public interface DownloadFileStatusCallback {

    void onReceived(@Nullable DownloadFileStatus downloadFileStatus);
}
