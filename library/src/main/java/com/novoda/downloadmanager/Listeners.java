package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;

class Listeners {

    private final List<OnDownloadsUpdateListener> listeners;

    public static Listeners newInstance() {
        return new Listeners(new ArrayList<OnDownloadsUpdateListener>());
    }

    Listeners(List<OnDownloadsUpdateListener> listeners) {
        this.listeners = listeners;
    }

    public void addOnDownloadsUpdateListener(OnDownloadsUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeOnDownloadsUpdateListener(OnDownloadsUpdateListener listener) {
        listeners.remove(listener);
    }

    public void notify(List<Download> downloads) {
        for (OnDownloadsUpdateListener listener : listeners) {
            listener.onDownloadsUpdate(downloads);
        }

    }

}
