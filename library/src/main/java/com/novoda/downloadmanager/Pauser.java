package com.novoda.downloadmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.novoda.downloadmanager.domain.DownloadId;

public class Pauser {

    private static final String ACTION_PAUSE = "PAUSE";
    private static final String EXTRA_DOWNLOAD_ID = "DOWNLOAD_ID";

    private final LocalBroadcastManager localBroadcastManager;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // null safe
        }
    };

    public Pauser(LocalBroadcastManager localBroadcastManager) {
        this.localBroadcastManager = localBroadcastManager;
    }

    public void requestPause(DownloadId downloadId) {
        localBroadcastManager.sendBroadcast(createPauseIntent(downloadId));
    }

    private Intent createPauseIntent(DownloadId downloadId) {
        return new Intent(ACTION_PAUSE).putExtra(EXTRA_DOWNLOAD_ID, downloadId.toString());
    }

    public void listenForPause(final DownloadId downloadId, final OnPauseListener onPauseListener) {
        receiver = getReceiver(downloadId, onPauseListener);
        localBroadcastManager.registerReceiver(receiver, new IntentFilter(ACTION_PAUSE));
    }

    private BroadcastReceiver getReceiver(final DownloadId downloadId, final OnPauseListener onPauseListener) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ACTION_PAUSE) && intent.getStringExtra(EXTRA_DOWNLOAD_ID).equals(downloadId.toString())) {
                    onPauseListener.onDownloadPaused();
                }
            }
        };
    }

    public void stopListeningForPause() {
        localBroadcastManager.unregisterReceiver(receiver);
    }

    public interface OnPauseListener {
        void onDownloadPaused();
    }

}
