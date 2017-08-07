package com.novoda.downloadmanager.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;

import com.novoda.downloadmanager.lib.DownloadManagerModules;
import com.novoda.downloadmanager.lib.PublicFacingDownloadMarshaller;
import com.novoda.downloadmanager.lib.PublicFacingStatusTranslator;

public class DownloadNotifierFactory {

    public DownloadNotifier getDownloadNotifier(Context context,
                                                DownloadManagerModules modules,
                                                PublicFacingDownloadMarshaller downloadMarshaller,
                                                PublicFacingStatusTranslator statusTranslator,
                                                NotificationManagerCustomiser customiser) {
        NotificationManager notificationManager = initNotificationManager(context, customiser);
        Resources resources = context.getResources();
        NotificationDisplayer notificationDisplayer = new NotificationDisplayer(
                context,
                notificationManager,
                modules.getNotificationImageRetriever(),
                resources,
                createNotificationCustomiser(modules),
                statusTranslator,
                downloadMarshaller
        );

        return new SynchronisedDownloadNotifier(context, notificationDisplayer);
    }

    private NotificationManager initNotificationManager(Context context, NotificationManagerCustomiser customiser) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return customiser.customise(notificationManager);
    }

    private NotificationCustomiser createNotificationCustomiser(DownloadManagerModules downloadManagerModules) {
        QueuedNotificationCustomiser queued = downloadManagerModules.getQueuedNotificationCustomiser();
        DownloadingNotificationCustomiser downloading = downloadManagerModules.getDownloadingNotificationCustomiser();
        CompleteNotificationCustomiser complete = downloadManagerModules.getCompleteNotificationCustomiser();
        CancelledNotificationCustomiser cancelled = downloadManagerModules.getCancelledNotificationCustomiser();
        FailedNotificationCustomiser failed = downloadManagerModules.getFailedNotificationCustomiser();

        return new NotificationCustomiser(queued, downloading, complete, cancelled, failed);
    }

}
