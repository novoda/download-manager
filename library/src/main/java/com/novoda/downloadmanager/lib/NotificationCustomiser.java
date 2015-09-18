package com.novoda.downloadmanager.lib;

import android.content.Intent;

public interface NotificationCustomiser {

    Intent createClickIntentForActiveBatch(DownloadBatch batch, String tag);
}
