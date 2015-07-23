package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.SystemClock;

import static com.novoda.downloadmanager.lib.DownloadContract.Downloads.COLUMN_CURRENT_BYTES;

class NotifierWriter implements DataWriter {

    private final ContentResolver contentResolver;
    private final DataWriter dataWriter;
    private final DownloadNotifier downloadNotifier;
    private final FileDownloadInfo downloadInfo;
    private final WriteChunkListener writeChunkListener;

    public NotifierWriter(ContentResolver contentResolver,
                          DataWriter dataWriter,
                          DownloadNotifier downloadNotifier,
                          FileDownloadInfo downloadInfo,
                          WriteChunkListener writeChunkListener) {
        this.contentResolver = contentResolver;
        this.dataWriter = dataWriter;
        this.downloadNotifier = downloadNotifier;
        this.downloadInfo = downloadInfo;
        this.writeChunkListener = writeChunkListener;
    }

    @Override
    public DownloadThread.State write(DownloadThread.State state, byte[] buffer, int count) throws StopRequestException {
        DownloadThread.State localState = state;
        localState = dataWriter.write(localState, buffer, count);
        localState = reportProgress(localState);
        writeChunkListener.chunkWritten(downloadInfo);
        return localState;
    }

    private DownloadThread.State reportProgress(DownloadThread.State state) {
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
                downloadNotifier.notifyDownloadSpeed(downloadInfo.getId(), state.speed);
            }

            state.speedSampleStart = now;
            state.speedSampleBytes = state.currentBytes;
        }

        if (state.currentBytes - state.bytesNotified > Constants.MIN_PROGRESS_STEP &&
                now - state.timeLastNotification > Constants.MIN_PROGRESS_TIME) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CURRENT_BYTES, state.currentBytes);
            contentResolver.update(downloadInfo.getAllDownloadsUri(), values, null, null);
            state.bytesNotified = state.currentBytes;
            state.timeLastNotification = now;
        }
        return state;
    }

    public interface WriteChunkListener {
        void chunkWritten(FileDownloadInfo downloadInfo) throws StopRequestException;
    }

}
