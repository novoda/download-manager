package com.novoda.downloadmanager;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

interface NotificationChannelProvider {

    @RequiresApi(Build.VERSION_CODES.O)
    void registerNotificationChannel(Context context);

    String channelId();
}
