package com.novoda.downloadmanager.lib;

import android.graphics.Bitmap;

public interface NotificationImageRetriever {

    /**
     * @param imageUrl The image wanted to be loaded.
     * @return Bitmap of the imageUrl or null if you fail.
     */
    Bitmap retrieveImage(String imageUrl);

}
