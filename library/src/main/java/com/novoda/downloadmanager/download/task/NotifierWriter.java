package com.novoda.downloadmanager.download.task;

import android.os.SystemClock;

import com.novoda.downloadmanager.domain.DownloadFile;
import com.novoda.downloadmanager.download.DownloadDatabaseWrapper;

class NotifierWriter implements DataWriter {

    /**
     * The minimum amount of progress that has to be done before the progress bar gets updated
     */
    public static final int MIN_PROGRESS_STEP = 4096;

    /**
     * The minimum amount of time that has to elapse before the progress bar gets updated, in ms
     */
    public static final long MIN_PROGRESS_TIME = 1500;

    private final DataWriter dataWriter;
    private final DownloadFile file;
    private final WriteChunkListener writeChunkListener;
    private final DownloadDatabaseWrapper downloadDatabaseWrapper;

    public NotifierWriter(DataWriter dataWriter,
                          DownloadFile file,
                          WriteChunkListener writeChunkListener,
                          DownloadDatabaseWrapper downloadDatabaseWrapper) {
        this.dataWriter = dataWriter;
        this.file = file;
        this.writeChunkListener = writeChunkListener;
        this.downloadDatabaseWrapper = downloadDatabaseWrapper;
    }

    @Override
    public State write(State state, byte[] buffer, int count) {
        State localState = state;
        localState = dataWriter.write(localState, buffer, count);
        localState = reportProgress(localState);
        writeChunkListener.chunkWritten(file);
        return localState;
    }

    private State reportProgress(State state) {
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
                // todo
//                downloadNotifier.notifyDownloadSpeed(file, state.speed);
            }

            state.speedSampleStart = now;
            state.speedSampleBytes = state.currentBytes;
        }

        if (state.currentBytes - state.bytesNotified > MIN_PROGRESS_STEP && now - state.timeLastNotification > MIN_PROGRESS_TIME) {
            downloadDatabaseWrapper.updateFileProgress(file, state.currentBytes);
            state.bytesNotified = state.currentBytes;
            state.timeLastNotification = now;
        }
        return state;
    }

    public interface WriteChunkListener {
        void chunkWritten(DownloadFile file);
    }

}
