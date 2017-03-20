package com.novoda.downloadmanager.lib;

import com.novoda.downloadmanager.notifications.DownloadNotifier;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class NotificationsUpdator {

    private static final ScheduledExecutorService notificationExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final boolean DO_NOT_INTERRUPT_IF_RUNNING = false;
    private static final int INITIAL_DELAY_IN_SECONDS = 2;
    private static final int REPEAT_EVERY_IN_SECONDS = 2;
    private static final TimeUnit TIME_UNIT_SECONDS = TimeUnit.SECONDS;

    private final DownloadNotifier downloadNotifier;
    private final DownloadsRepository downloadsRepository;
    private final BatchRepository batchRepository;

    private ScheduledFuture notification;

    public NotificationsUpdator(DownloadNotifier downloadNotifier, DownloadsRepository downloadsRepository, BatchRepository batchRepository) {
        this.downloadNotifier = downloadNotifier;
        this.downloadsRepository = downloadsRepository;
        this.batchRepository = batchRepository;
    }

    public void startUpdating() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                List<DownloadBatch> downloadBatches = getDownloadBatches();
                updateImmediately(downloadBatches);
            }
        };

        stopUpdating();
        notification = notificationExecutor.scheduleAtFixedRate(task, INITIAL_DELAY_IN_SECONDS, REPEAT_EVERY_IN_SECONDS, TIME_UNIT_SECONDS);
    }

    private List<DownloadBatch> getDownloadBatches() {
        Collection<FileDownloadInfo> allDownloads = downloadsRepository.getAllDownloads();
        return batchRepository.retrieveBatchesFor(allDownloads);
    }

    public void stopUpdating() {
        if (notification != null) {
            notification.cancel(DO_NOT_INTERRUPT_IF_RUNNING);
        }
    }

    public void updateImmediately(final List<DownloadBatch> downloadBatches) {
        downloadNotifier.updateWith(downloadBatches);
    }

    public void updateDownloadSpeed(long id, long speed) {
        downloadNotifier.notifyDownloadSpeed(id, speed);
    }
}
