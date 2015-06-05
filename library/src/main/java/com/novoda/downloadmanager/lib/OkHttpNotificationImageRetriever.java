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
                .url(this.imageUrl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            try {
                this.imageUrl = imageUrl;
                cleanupOldBitmap();
                bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            return null;
        }
    }

    private void cleanupOldBitmap() {
        if (bitmap == null) {
            return;
        }
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        bitmap = null;
    }
}
