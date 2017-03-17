package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.SystemClock;

import static com.novoda.downloadmanager.lib.DownloadContract.Downloads.COLUMN_CURRENT_BYTES;

class NotifierWriter implements DataWriter {

    private final ContentResolver contentResolver;
    private final DataWriter dataWriter;
    private final NotificationsUpdator notificationsUpdator;
    private final FileDownloadInfo downloadInfo;
    private final WriteChunkListener writeChunkListener;

    private final ContentValues values = new ContentValues();

    public NotifierWriter(ContentResolver contentResolver,
                          DataWriter dataWriter,
                          NotificationsUpdator notificationsUpdator,
                          FileDownloadInfo downloadInfo,
                          WriteChunkListener writeChunkListener) {
        this.contentResolver = contentResolver;
        this.dataWriter = dataWriter;
        this.notificationsUpdator = notificationsUpdator;
        this.downloadInfo = downloadInfo;
        this.writeChunkListener = writeChunkListener;
    }

    @Override
    public DownloadTask.State write(DownloadTask.State state, byte[] buffer, int count) throws StopRequestException {
        DownloadTask.State localState = state;
        localState = dataWriter.write(localState, buffer, count);
        localState = reportProgress(localState);
        writeChunkListener.chunkWritten(downloadInfo);
        return localState;
    }

    private DownloadTask.State reportProgress(DownloadTask.State state) {
        final long now = SystemClock.elapsedRealtime();

        final long sampleDelta = now - state.speedSampleStart;
        if (sampleDelta > 500) {
            final long sampleSpeed = ((state.currentBytes - state.speedSampleBytes) * 1000) / sampleDelta;

            if (state.speed == 0) {
                state.speed = sampleSpeed;
            } else {
                state.speed = ((state.speed * 3) + sampleSpeed) / 4;
            }

            // Only notify once we have a full sample window
            if (state.speedSampleStart != 0) {
                notificationsUpdator.updateDownloadSpeed(downloadInfo.getId(), state.speed);
            }

            state.speedSampleStart = now;
            state.speedSampleBytes = state.currentBytes;
        }

        if (state.currentBytes - state.bytesNotified > Constants.MIN_PROGRESS_STEP &&
                now - state.timeLastNotification > Constants.MIN_PROGRESS_TIME) {
            updateCurrentBytesValues(state);
            contentResolver.update(downloadInfo.getAllDownloadsUri(), values, null, null);
            state.bytesNotified = state.currentBytes;
            state.timeLastNotification = now;
        }
        return state;
    }

    private void updateCurrentBytesValues(DownloadTask.State state) {
        values.put(COLUMN_CURRENT_BYTES, state.currentBytes);
    }

    interface WriteChunkListener {

        void chunkWritten(FileDownloadInfo downloadInfo) throws StopRequestException;
    }

}
