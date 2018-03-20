package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.novoda.merlin.MerlinsBeard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DELETING;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.DOWNLOADED;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.ERROR;
import static com.novoda.downloadmanager.DownloadBatchStatus.Status.PAUSED;

@SuppressWarnings("PMD.ExcessiveImports")
public final class DownloadManagerBuilder {

    private static final Object SERVICE_LOCK = new Object();
    private static final Object CALLBACK_LOCK = new Object();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Context applicationContext;
    private final Handler callbackHandler;

    private FilePersistenceCreator filePersistenceCreator;
    private FileSizeRequester fileSizeRequester;
    private FileDownloaderCreator fileDownloaderCreator;
    private DownloadService downloadService;
    private DownloadManager downloadManager;
    private NotificationCreator<DownloadBatchStatus> notificationCreator;
    private NotificationChannelProvider notificationChannelProvider;
    private ConnectionType connectionTypeAllowed;
    private boolean allowNetworkRecovery;
    private Class<? extends CallbackThrottle> customCallbackThrottle;
    private DownloadsPersistence downloadsPersistence;
    private CallbackThrottleCreator.Type callbackThrottleCreatorType;
    private TimeUnit timeUnit;
    private long frequency;
    private Optional<LogHandle> logHandle;

    public static DownloadManagerBuilder newInstance(Context context, Handler callbackHandler, @DrawableRes final int notificationIcon) {
        Context applicationContext = context.getApplicationContext();

        FilePersistenceCreator filePersistenceCreator = FilePersistenceCreator.newInternalFilePersistenceCreator(applicationContext);
        FileDownloaderCreator fileDownloaderCreator = FileDownloaderCreator.newNetworkFileDownloaderCreator();

        NetworkRequestCreator requestCreator = new NetworkRequestCreator();
        HttpClient httpClient = HttpClientFactory.getInstance();
        FileSizeRequester fileSizeRequester = new NetworkFileSizeRequester(httpClient, requestCreator);

        DownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(applicationContext);

        NotificationChannelProvider notificationChannelProvider = new DefaultNotificationChannelProvider(
                context.getResources().getString(R.string.download_notification_channel_name),
                context.getResources().getString(R.string.download_notification_channel_description),
                NotificationManagerCompat.IMPORTANCE_LOW
        );
        NotificationCustomizer<DownloadBatchStatus> notificationCustomizer = new DownloadNotificationCustomizer(
                context.getResources(),
                notificationIcon
        );
        NotificationCreator<DownloadBatchStatus> notificationCreator = new DownloadBatchStatusNotificationCreator(
                context,
                notificationCustomizer,
                notificationChannelProvider
        );

        ConnectionType connectionTypeAllowed = ConnectionType.ALL;
        boolean allowNetworkRecovery = true;

        CallbackThrottleCreator.Type callbackThrottleCreatorType = CallbackThrottleCreator.Type.THROTTLE_BY_PROGRESS_INCREASE;

        Optional<LogHandle> logHandle = Optional.absent();

        return new DownloadManagerBuilder(
                applicationContext,
                callbackHandler,
                filePersistenceCreator,
                downloadsPersistence,
                fileSizeRequester,
                fileDownloaderCreator,
                notificationChannelProvider,
                notificationCreator,
                connectionTypeAllowed,
                allowNetworkRecovery,
                callbackThrottleCreatorType,
                logHandle
        );
    }

    @SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})     // Can't group anymore these are customisable options.
    private DownloadManagerBuilder(Context applicationContext,
                                   Handler callbackHandler,
                                   FilePersistenceCreator filePersistenceCreator,
                                   DownloadsPersistence downloadsPersistence,
                                   FileSizeRequester fileSizeRequester,
                                   FileDownloaderCreator fileDownloaderCreator,
                                   NotificationChannelProvider notificationChannelProvider,
                                   NotificationCreator<DownloadBatchStatus> notificationCreator,
                                   ConnectionType connectionTypeAllowed,
                                   boolean allowNetworkRecovery,
                                   CallbackThrottleCreator.Type callbackThrottleCreatorType,
                                   Optional<LogHandle> logHandle) {
        this.applicationContext = applicationContext;
        this.callbackHandler = callbackHandler;
        this.filePersistenceCreator = filePersistenceCreator;
        this.downloadsPersistence = downloadsPersistence;
        this.fileSizeRequester = fileSizeRequester;
        this.fileDownloaderCreator = fileDownloaderCreator;
        this.notificationChannelProvider = notificationChannelProvider;
        this.notificationCreator = notificationCreator;
        this.connectionTypeAllowed = connectionTypeAllowed;
        this.allowNetworkRecovery = allowNetworkRecovery;
        this.callbackThrottleCreatorType = callbackThrottleCreatorType;
        this.logHandle = logHandle;
    }

    public DownloadManagerBuilder withFilePersistenceInternal() {
        filePersistenceCreator = FilePersistenceCreator.newInternalFilePersistenceCreator(applicationContext);
        return this;
    }

    public DownloadManagerBuilder withFilePersistenceExternal() {
        filePersistenceCreator = FilePersistenceCreator.newExternalFilePersistenceCreator(applicationContext);
        return this;
    }

    public DownloadManagerBuilder withFileDownloaderCustom(FileSizeRequester fileSizeRequester,
                                                           Class<? extends FileDownloader> customFileDownloaderClass) {
        this.fileSizeRequester = fileSizeRequester;
        this.fileDownloaderCreator = FileDownloaderCreator.newCustomFileDownloaderCreator(customFileDownloaderClass);
        return this;
    }

    public DownloadManagerBuilder withFilePersistenceCustom(Class<? extends FilePersistence> customFilePersistenceClass) {
        filePersistenceCreator = FilePersistenceCreator.newCustomFilePersistenceCreator(applicationContext, customFilePersistenceClass);
        return this;
    }

    public DownloadManagerBuilder withDownloadsPersistenceCustom(DownloadsPersistence downloadsPersistence) {
        this.downloadsPersistence = downloadsPersistence;
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public DownloadManagerBuilder withNotificationChannel(NotificationChannel notificationChannel) {
        this.notificationChannelProvider = new OreoNotificationChannelProvider(notificationChannel);
        this.notificationCreator.setNotificationChannelProvider(notificationChannelProvider);
        return this;
    }

    public DownloadManagerBuilder withNotificationChannel(String channelId, String name, @Importance int importance) {
        this.notificationChannelProvider = new DefaultNotificationChannelProvider(channelId, name, importance);
        this.notificationCreator.setNotificationChannelProvider(notificationChannelProvider);
        return this;
    }

    public DownloadManagerBuilder withNotification(NotificationCustomizer<DownloadBatchStatus> notificationCustomizer) {
        this.notificationCreator = new DownloadBatchStatusNotificationCreator(
                applicationContext,
                notificationCustomizer,
                notificationChannelProvider
        );
        return this;
    }

    public DownloadManagerBuilder withAllowedConnectionType(ConnectionType connectionTypeAllowed) {
        this.connectionTypeAllowed = connectionTypeAllowed;
        return this;
    }

    public DownloadManagerBuilder withoutNetworkRecovery() {
        allowNetworkRecovery = false;
        return this;
    }

    public DownloadManagerBuilder withCallbackThrottleCustom(Class<? extends CallbackThrottle> customCallbackThrottle) {
        this.callbackThrottleCreatorType = CallbackThrottleCreator.Type.CUSTOM;
        this.customCallbackThrottle = customCallbackThrottle;
        return this;
    }

    public DownloadManagerBuilder withCallbackThrottleByTime(TimeUnit timeUnit, long frequency) {
        this.callbackThrottleCreatorType = CallbackThrottleCreator.Type.THROTTLE_BY_TIME;
        this.timeUnit = timeUnit;
        this.frequency = frequency;
        return this;
    }

    public DownloadManagerBuilder withCallbackThrottleByProgressIncrease() {
        this.callbackThrottleCreatorType = CallbackThrottleCreator.Type.THROTTLE_BY_PROGRESS_INCREASE;
        return this;
    }

    public DownloadManagerBuilder withLogHandle(LogHandle logHandle) {
        this.logHandle = Optional.fromNullable(logHandle);
        return this;
    }

    public DownloadManager build() {
        if (logHandle.isPresent()) {
            Logger.attach(logHandle.get());
        }

        Intent intent = new Intent(applicationContext, LiteDownloadService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LiteDownloadService.DownloadServiceBinder binder = (LiteDownloadService.DownloadServiceBinder) service;
                downloadService = binder.getService();
                downloadManager.submitAllStoredDownloads(() -> {
                    downloadManager.initialise(downloadService);

                    if (allowNetworkRecovery) {
                        DownloadsNetworkRecoveryCreator.createEnabled(applicationContext, downloadManager, connectionTypeAllowed);
                    } else {
                        DownloadsNetworkRecoveryCreator.createDisabled();
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // no-op
            }
        };

        applicationContext.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

        FileOperations fileOperations = new FileOperations(filePersistenceCreator, fileSizeRequester, fileDownloaderCreator);
        List<DownloadBatchStatusCallback> callbacks = new ArrayList<>();

        CallbackThrottleCreator callbackThrottleCreator = getCallbackThrottleCreator(
                callbackThrottleCreatorType,
                timeUnit,
                frequency,
                customCallbackThrottle
        );

        DownloadsFilePersistence downloadsFilePersistence = new DownloadsFilePersistence(downloadsPersistence);
        MerlinsBeard merlinsBeard = MerlinsBeard.from(applicationContext);
        ConnectionChecker connectionChecker = new ConnectionChecker(merlinsBeard, connectionTypeAllowed);
        Executor executor = Executors.newSingleThreadExecutor();
        DownloadsBatchPersistence downloadsBatchPersistence = new DownloadsBatchPersistence(
                executor,
                downloadsFilePersistence,
                downloadsPersistence,
                callbackThrottleCreator,
                connectionChecker
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelProvider.registerNotificationChannel(applicationContext);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
        ServiceNotificationDispatcher<DownloadBatchStatus> notificationDispatcher = new ServiceNotificationDispatcher<>(
                SERVICE_LOCK,
                notificationCreator,
                notificationManager
        );
        DownloadBatchStatusNotificationDispatcher batchStatusNotificationDispatcher = new DownloadBatchStatusNotificationDispatcher(
                downloadsBatchPersistence,
                notificationDispatcher,
                new HashSet<>()
        );

        LiteDownloadManagerDownloader downloader = new LiteDownloadManagerDownloader(
                SERVICE_LOCK,
                CALLBACK_LOCK,
                EXECUTOR,
                callbackHandler,
                fileOperations,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                batchStatusNotificationDispatcher,
                connectionChecker,
                callbacks,
                callbackThrottleCreator
        );

        downloadManager = new DownloadManager(
                SERVICE_LOCK,
                CALLBACK_LOCK,
                EXECUTOR,
                callbackHandler,
                new HashMap<>(),
                callbacks,
                fileOperations,
                downloadsBatchPersistence,
                downloader,
                connectionChecker
        );

        return downloadManager;
    }

    private CallbackThrottleCreator getCallbackThrottleCreator(CallbackThrottleCreator.Type callbackThrottleType,
                                                               TimeUnit timeUnit,
                                                               long frequency,
                                                               Class<? extends CallbackThrottle> customCallbackThrottle) {
        switch (callbackThrottleType) {
            case THROTTLE_BY_TIME:
                return CallbackThrottleCreator.byTime(timeUnit, frequency);
            case THROTTLE_BY_PROGRESS_INCREASE:
                return CallbackThrottleCreator.byProgressIncrease();
            case CUSTOM:
                return CallbackThrottleCreator.byCustomThrottle(customCallbackThrottle);
            default:
                throw new IllegalStateException("callbackThrottle type " + callbackThrottleType + " not implemented yet");
        }
    }

    private static class DownloadNotificationCustomizer implements NotificationCustomizer<DownloadBatchStatus> {

        private static final boolean NOT_INDETERMINATE = false;
        private final Resources resources;
        private final int notificationIcon;

        DownloadNotificationCustomizer(Resources resources, int notificationIcon) {
            this.resources = resources;
            this.notificationIcon = notificationIcon;
        }

        @Override
        public NotificationDisplayState notificationDisplayState(DownloadBatchStatus payload) {
            DownloadBatchStatus.Status status = payload.status();
            if (status == DOWNLOADED || status == DELETED || status == DELETING || status == ERROR || status == PAUSED) {
                return NotificationDisplayState.STACK_NOTIFICATION_DISMISSIBLE;
            } else {
                return NotificationDisplayState.SINGLE_PERSISTENT_NOTIFICATION;
            }
        }

        @Override
        public Notification customNotificationFrom(NotificationCompat.Builder builder, DownloadBatchStatus payload) {
            DownloadBatchTitle downloadBatchTitle = payload.getDownloadBatchTitle();
            String title = downloadBatchTitle.asString();
            builder.setSmallIcon(notificationIcon)
                    .setContentTitle(title);

            switch (payload.status()) {
                case DELETED:
                case DELETING:
                    return createDeletedNotification(builder);
                case ERROR:
                    return createErrorNotification(builder, payload.getDownloadErrorType());
                case DOWNLOADED:
                    return createCompletedNotification(builder);
                default:
                    return createProgressNotification(builder, payload);
            }
        }

        private Notification createDeletedNotification(NotificationCompat.Builder builder) {
            String content = resources.getString(R.string.download_notification_content_deleted);
            return builder
                    .setContentText(content)
                    .build();
        }

        private Notification createErrorNotification(NotificationCompat.Builder builder, DownloadError.Error errorType) {
            String content = resources.getString(R.string.download_notification_content_error, errorType);
            return builder
                    .setContentText(content)
                    .build();
        }

        private Notification createCompletedNotification(NotificationCompat.Builder builder) {
            String content = resources.getString(R.string.download_notification_content_completed);
            return builder
                    .setContentText(content)
                    .build();
        }

        private Notification createProgressNotification(NotificationCompat.Builder builder, DownloadBatchStatus payload) {
            int bytesFileSize = (int) payload.bytesTotalSize();
            int bytesDownloaded = (int) payload.bytesDownloaded();
            String content = resources.getString(R.string.download_notification_content_progress, payload.percentageDownloaded());

            return builder
                    .setProgress(bytesFileSize, bytesDownloaded, NOT_INDETERMINATE)
                    .setContentText(content)
                    .build();
        }

    }
}
