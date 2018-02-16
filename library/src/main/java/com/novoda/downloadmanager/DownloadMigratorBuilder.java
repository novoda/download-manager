package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.novoda.notils.logger.simple.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DownloadMigratorBuilder {

    private static final Object LOCK = new Object();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Context applicationContext;
    private final Handler handler;

    private NotificationChannelProvider notificationChannelProvider;
    private NotificationCreator<MigrationStatus> notificationCreator;
    private LiteDownloadMigrator downloadMigrator;
    private MigrationCallback migrationCallback;

    public static DownloadMigratorBuilder newInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        Resources resources = context.getResources();

        DefaultNotificationChannelProvider notificationChannelProvider = new DefaultNotificationChannelProvider(
                resources.getString(R.string.download_notification_channel_name),
                resources.getString(R.string.download_notification_channel_description),
                NotificationManagerCompat.IMPORTANCE_LOW
        );
        NotificationCustomizer<MigrationStatus> customizer = new MigrationNotificationCustomizer(context.getResources());
        NotificationCreator<MigrationStatus> defaultNotificationCreator = new NotificationCreator<>(
                applicationContext,
                customizer,
                notificationChannelProvider
        );
        Handler handler = new Handler(Looper.getMainLooper());
        MigrationCallback migrationCallback = (status) -> {
            Log.v(status.toString());
        };
        return new DownloadMigratorBuilder(applicationContext, handler, notificationChannelProvider, defaultNotificationCreator, migrationCallback);
    }

    private DownloadMigratorBuilder(Context applicationContext,
                                    Handler handler,
                                    NotificationChannelProvider notificationChannelProvider,
                                    NotificationCreator<MigrationStatus> notificationCreator,
                                    MigrationCallback migrationCallback) {
        this.applicationContext = applicationContext;
        this.handler = handler;
        this.notificationChannelProvider = notificationChannelProvider;
        this.notificationCreator = notificationCreator;
        this.migrationCallback = migrationCallback;
    }

    public DownloadMigratorBuilder withMigrationCallback(MigrationCallback migrationCallback) {
        this.migrationCallback = migrationCallback;
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public DownloadMigratorBuilder withNotificationChannel(NotificationChannel notificationChannel) {
        this.notificationChannelProvider = new OreoNotificationChannelProvider(notificationChannel);
        this.notificationCreator.setNotificationChannelProvider(notificationChannelProvider);
        return this;
    }

    public DownloadMigratorBuilder withNotificationChannel(String channelId, String name, @Importance int importance) {
        this.notificationChannelProvider = new DefaultNotificationChannelProvider(channelId, name, importance);
        this.notificationCreator.setNotificationChannelProvider(notificationChannelProvider);
        return this;
    }

    public DownloadMigratorBuilder withNotification(NotificationCustomizer<MigrationStatus> notificationCustomizer) {
        this.notificationCreator = new NotificationCreator<>(applicationContext, notificationCustomizer, notificationChannelProvider);
        return this;
    }

    public DownloadMigrator build() {
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                DownloadMigrationService migrationService = ((LiteDownloadMigrationService.MigrationDownloadServiceBinder) binder).getService();
                downloadMigrator.initialise(migrationService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // do nothing.
            }
        };

        Intent serviceIntent = new Intent(applicationContext, LiteDownloadMigrationService.class);
        applicationContext.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
        ServiceNotificationDispatcher<MigrationStatus> notificationDispatcher = new ServiceNotificationDispatcher<>(
                LOCK,
                notificationCreator,
                notificationManager
        );

        downloadMigrator = new LiteDownloadMigrator(applicationContext, LOCK, EXECUTOR, handler, migrationCallback, notificationDispatcher);
        return downloadMigrator;
    }

    private static class MigrationNotificationCustomizer implements NotificationCustomizer<MigrationStatus> {
        private static final int MAX_PROGRESS = 100;

        private final Resources resources;

        MigrationNotificationCustomizer(Resources resources) {
            this.resources = resources;
        }

        @Override
        public NotificationStackState notificationStackState(MigrationStatus payload) {
            MigrationStatus.Status status = payload.status();

            if (status == MigrationStatus.Status.COMPLETE || status == MigrationStatus.Status.DB_NOT_PRESENT) {
                return NotificationStackState.STACK_NOTIFICATION_DISMISSIBLE;
            } else {
                return NotificationStackState.SINGLE_PERSISTENT_NOTIFICATION;
            }
        }

        @Override
        public Notification customNotificationFrom(NotificationCompat.Builder builder, MigrationStatus payload) {
            String title = payload.status().toRawValue();
            String content = resources.getString(R.string.migration_notification_content_progress, payload.percentageMigrated());
            return builder
                    .setProgress(MAX_PROGRESS, payload.percentageMigrated(), false)
                    .setSmallIcon(android.R.drawable.ic_menu_gallery)
                    .setContentTitle(title)
                    .setContentText(content)
                    .build();
        }
    }
}
