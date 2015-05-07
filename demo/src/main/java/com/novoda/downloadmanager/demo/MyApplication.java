package com.novoda.downloadmanager.demo;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.novoda.downloadmanager.lib.NotificationImageRetriever;
import com.novoda.downloadmanager.lib.NotificationImageRetrieverFactory;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

public class MyApplication extends Application implements NotificationImageRetrieverFactory {

    private final OkHttpNotificationImageRetriever imageRetriever = new OkHttpNotificationImageRetriever();

    @Override
    public NotificationImageRetriever createNotificationImageRetriever() {
        return imageRetriever;
    }

    private static class OkHttpNotificationImageRetriever implements NotificationImageRetriever {

        private final OkHttpClient client;

        public OkHttpNotificationImageRetriever() {
            client = new OkHttpClient();
        }

        @Override
        public Bitmap retrieveImage(String imageUrl) {
            Request request = new Request.Builder()
                    .get()
                    .url(imageUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                InputStream inputStream = response.body().byteStream();
                try {
                    return BitmapFactory.decodeStream(inputStream);
                } finally {
                    inputStream.close();
                }
            } catch (IOException e) {
                return null;
            }
        }
    }

}
