package com.novoda.downloadmanager.lib;

import android.content.Intent;

public interface NotificationCustomiser {

    Intent createClickIntentForActiveBatch(long batchId, String tag);
}
