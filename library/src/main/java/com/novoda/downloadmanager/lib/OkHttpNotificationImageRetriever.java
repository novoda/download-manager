package com.novoda.downloadmanager.lib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

class OkHttpNotificationImageRetriever implements NotificationImageRetriever {

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
