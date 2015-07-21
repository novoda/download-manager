package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.SystemClock;

import static com.novoda.downloadmanager.lib.DownloadContract.Downloads.COLUMN_CURRENT_BYTES;

public class NotifierWriter implements DataWriter {

    private final ContentResolver contentResolver;
    private final DataWriter dataWriter;
    private final DownloadNotifier downloadNotifier;
    private final DownloadsRepository downloadsRepository;
    private final FileDownloadInfo downloadInfo;

    public NotifierWriter(ContentResolver contentResolver,
                          DataWriter dataWriter,
                          DownloadNotifier downloadNotifier,
                          DownloadsRepository downloadsRepository,
                          FileDownloadInfo downloadInfo) {
        this.contentResolver = contentResolver;
        this.dataWriter = dataWriter;
        this.downloadNotifier = downloadNotifier;
        this.downloadsRepository = downloadsRepository;
        this.downloadInfo = downloadInfo;
    }

    @Override
    public DownloadThread.State write(DownloadThread.State state, byte[] buffer, int count) throws StopRequestException {
        DownloadThread.State localState = state;
        localState = dataWriter.write(localState, buffer, count);
        localState = reportProgress(localState);
        checkPausedOrCanceled();
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

    /**
     * Check if the download has been paused or canceled, stopping the request appropriately if it
     * has been.
     */
    private void checkPausedOrCanceled() throws StopRequestException {
        FileDownloadInfo.ControlStatus controlStatus = downloadsRepository.getDownloadInfoControlStatusFor(downloadInfo.getId());

        if (controlStatus.isPaused()) {
            throw new StopRequestException(DownloadStatus.PAUSED_BY_APP, "download paused by owner");
        }
        if (controlStatus.isCanceled()) {
            throw new StopRequestException(DownloadStatus.CANCELED, "download canceled");
        }
    }

}
