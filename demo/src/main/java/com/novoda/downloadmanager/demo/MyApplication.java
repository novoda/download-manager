package com.novoda.downloadmanager.demo;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.novoda.downloadmanager.lib.NotificationImageRetriever;
import com.novoda.downloadmanager.lib.NotificationImageRetrieverFactory;

public class MyApplication extends Application implements NotificationImageRetrieverFactory {

    @Override
    public NotificationImageRetriever createNotificationImageRetriever() {
        return new NotificationImageRetriever() {
            @Override
            public Bitmap retrieveImage(String imageUrl) {
                return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            }
        };
    }

}
