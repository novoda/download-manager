package com.novoda.downloadmanager.lib;

import android.graphics.Bitmap;

public interface ImageRetriever {
    Bitmap retrieve(String imageUrl);
}
