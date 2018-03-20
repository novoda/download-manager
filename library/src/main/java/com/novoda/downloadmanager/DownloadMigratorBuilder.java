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
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

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

    public static DownloadMigratorBuilder newInstance(Context context, @DrawableRes int notificationIcon) {
        Context applicationContext = context.getApplicationContext();
        Resources resources = context.getResources();

        DefaultNotificationChannelProvider notificationChannelProvider = new DefaultNotificationChannelProvider(
                resources.getString(R.string.download_notification_channel_name),
                resources.getString(R.string.download_notification_channel_description),
                NotificationManagerCompat.IMPORTANCE_LOW
        );
        NotificationCustomizer<MigrationStatus> customizer = new MigrationNotificationCustomizer(context.getResources(), notificationIcon);
        NotificationCreator<MigrationStatus> defaultNotificationCreator = new MigrationStatusNotificationCreator(
                applicationContext,
                customizer,
                notificationChannelProvider
        );
        Handler handler = new Handler(Looper.getMainLooper());
        MigrationCallback migrationCallback = (status) -> {
            Logger.v(status.toString());
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
        this.notificationCreator = new MigrationStatusNotificationCreator(applicationContext, notificationCustomizer, notificationChannelProvider);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelProvider.registerNotificationChannel(applicationContext);
        }

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

        private static final boolean NOT_INDETERMINATE = false;
        private static final int MAX_PROGRESS = 100;

        private final Resources resources;
        private final int notificationIcon;

        MigrationNotificationCustomizer(Resources resources, int notificationIcon) {
            this.resources = resources;
            this.notificationIcon = notificationIcon;
        }

        @Override
        public NotificationDisplayState notificationDisplayState(MigrationStatus migrationStatus) {
            MigrationStatus.Status status = migrationStatus.status();

            if (status == MigrationStatus.Status.COMPLETE) {
                return NotificationDisplayState.STACK_NOTIFICATION_DISMISSIBLE;
            } else if (status == MigrationStatus.Status.DB_NOT_PRESENT) {
                return NotificationDisplayState.HIDDEN_NOTIFICATION;
            } else {
                return NotificationDisplayState.SINGLE_PERSISTENT_NOTIFICATION;
            }
        }

        @Override
        public Notification customNotificationFrom(NotificationCompat.Builder builder, MigrationStatus migrationStatus) {
            String title = resources.getString(R.string.migration_notification_content_title);
            builder.setSmallIcon(notificationIcon)
                    .setContentTitle(title);

            if (migrationStatus.status() == MigrationStatus.Status.COMPLETE) {
                return createCompletedNotification(builder);
            } else {
                return createProgressNotification(builder, migrationStatus);
            }
        }

        private Notification createCompletedNotification(NotificationCompat.Builder builder) {
            String content = resources.getString(R.string.download_notification_content_completed);
            return builder
                    .setContentText(content)
                    .build();
        }

        private Notification createProgressNotification(NotificationCompat.Builder builder, MigrationStatus migrationStatus) {
            int percentageMigrated = migrationStatus.percentageMigrated();
            String content = resources.getString(R.string.download_notification_content_progress, percentageMigrated);

            return builder
                    .setProgress(MAX_PROGRESS, percentageMigrated, NOT_INDETERMINATE)
                    .setContentText(content)
                    .build();
        }
    }

}
