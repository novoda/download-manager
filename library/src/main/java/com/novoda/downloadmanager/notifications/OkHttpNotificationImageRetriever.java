package com.novoda.downloadmanager.notifications;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// TODO make this package - when everything notification-like is moved to /notifications/
public class OkHttpNotificationImageRetriever implements NotificationImageRetriever {

    private final OkHttpClient client;

    private String imageUrl;
    private Bitmap bitmap;

    public OkHttpNotificationImageRetriever() {
        client = new OkHttpClient();
    }

    @Override
    public Bitmap retrieveImage(String imageUrl) {
        if (imageUrl.equals(this.imageUrl)) {
            return bitmap;
        }
        return fetchBitmap(imageUrl);
    }

    private Bitmap fetchBitmap(String imageUrl) {
        Request request = new Request.Builder()
                .get()
                .url(imageUrl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            try {
                bitmap = BitmapFactory.decodeStream(inputStream);
                this.imageUrl = imageUrl;
                return bitmap;
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            return null;
        }
    }
}
