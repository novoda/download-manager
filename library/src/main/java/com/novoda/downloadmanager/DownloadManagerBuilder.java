package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;

import com.novoda.notils.logger.simple.Log;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class DownloadManagerBuilder {

    private static final Object LOCK = new Object();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Context applicationContext;
    private final Handler callbackHandler;

    private FilePersistenceCreator filePersistenceCreator;
    private FileSizeRequester fileSizeRequester;
    private FileDownloader fileDownloader;
    private DownloadService downloadService;
    private DownloadManager downloadManager;
    private NotificationConfig<DownloadBatchStatus> notificationConfig;
    private ConnectionType connectionTypeAllowed;
    private boolean allowNetworkRecovery;
    private Class<? extends CallbackThrottle> customCallbackThrottle;
    private DownloadsPersistence downloadsPersistence;
    private CallbackThrottleCreator.Type callbackThrottleCreatorType;
    private TimeUnit timeUnit;
    private long frequency;

    public static DownloadManagerBuilder newInstance(Context context, Handler callbackHandler, @DrawableRes final int notificationIcon) {
        Log.setShowLogs(true);
        Context applicationContext = context.getApplicationContext();

        // File persistence
        FilePersistenceCreator filePersistenceCreator = FilePersistenceCreator.newInternalFilePersistenceCreator(applicationContext);

        // Downloads information persistence
        DownloadsPersistence downloadsPersistence = RoomDownloadsPersistence.newInstance(applicationContext);

        // Network downloader
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        okHttpClient.setWriteTimeout(5, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(5, TimeUnit.SECONDS);
        HttpClient httpClient = new WrappedOkHttpClient(okHttpClient);

        NetworkRequestCreator requestCreator = new NetworkRequestCreator();
        FileSizeRequester fileSizeRequester = new NetworkFileSizeRequester(httpClient, requestCreator);
        FileDownloader fileDownloader = new NetworkFileDownloader(httpClient, requestCreator);

        NotificationCustomiser<DownloadBatchStatus> notificationCustomiser = new NotificationCustomiser<DownloadBatchStatus>() {

            private static final boolean NOT_INDETERMINATE = false;

            @Override
            public Notification customNotificationFrom(NotificationCompat.Builder builder, DownloadBatchStatus payload) {
                DownloadBatchTitle downloadBatchTitle = payload.getDownloadBatchTitle();
                int percentageDownloaded = payload.percentageDownloaded();
                int bytesFileSize = (int) payload.bytesTotalSize();
                int bytesDownloaded = (int) payload.bytesDownloaded();
                String title = downloadBatchTitle.asString();
                String content = percentageDownloaded + "% downloaded";

                return builder
                        .setProgress(bytesFileSize, bytesDownloaded, NOT_INDETERMINATE)
                        .setSmallIcon(notificationIcon)
                        .setContentTitle(title)
                        .setContentText(content)
                        .build();

            }
        };
        NotificationConfig<DownloadBatchStatus> notificationConfig = new NotificationConfig<>(
                context,
                context.getResources().getString(R.string.download_notification_channel_name),
                context.getResources().getString(R.string.download_notification_channel_description),
                notificationCustomiser,
                NotificationManager.IMPORTANCE_LOW
        );

        ConnectionType connectionTypeAllowed = ConnectionType.ALL;
        boolean allowNetworkRecovery = true;

        CallbackThrottleCreator.Type callbackThrottleCreatorType = CallbackThrottleCreator.Type.THROTTLE_BY_PROGRESS_INCREASE;

        return new DownloadManagerBuilder(
                applicationContext,
                callbackHandler,
                filePersistenceCreator,
                downloadsPersistence,
                fileSizeRequester,
                fileDownloader,
                notificationConfig,
                connectionTypeAllowed,
                allowNetworkRecovery,
                callbackThrottleCreatorType
        );
    }

    private DownloadManagerBuilder(Context applicationContext,
                                   Handler callbackHandler,
                                   FilePersistenceCreator filePersistenceCreator,
                                   DownloadsPersistence downloadsPersistence,
                                   FileSizeRequester fileSizeRequester,
                                   FileDownloader fileDownloader,
                                   NotificationConfig<DownloadBatchStatus> notificationConfig,
                                   ConnectionType connectionTypeAllowed,
                                   boolean allowNetworkRecovery,
                                   CallbackThrottleCreator.Type callbackThrottleCreatorType) {
        this.applicationContext = applicationContext;
        this.callbackHandler = callbackHandler;
        this.filePersistenceCreator = filePersistenceCreator;
        this.downloadsPersistence = downloadsPersistence;
        this.fileSizeRequester = fileSizeRequester;
        this.fileDownloader = fileDownloader;
        this.notificationConfig = notificationConfig;
        this.connectionTypeAllowed = connectionTypeAllowed;
        this.allowNetworkRecovery = allowNetworkRecovery;
        this.callbackThrottleCreatorType = callbackThrottleCreatorType;
    }

    public DownloadManagerBuilder withFilePersistenceInternal() {
        filePersistenceCreator = FilePersistenceCreator.newInternalFilePersistenceCreator(applicationContext);
        return this;
    }

    public DownloadManagerBuilder withFilePersistenceExternal() {
        filePersistenceCreator = FilePersistenceCreator.newExternalFilePersistenceCreator(applicationContext);
        return this;
    }

    public DownloadManagerBuilder withFileDownloaderCustom(FileSizeRequester fileSizeRequester, FileDownloader fileDownloader) {
        this.fileSizeRequester = fileSizeRequester;
        this.fileDownloader = fileDownloader;
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

    public DownloadManagerBuilder withNotification(NotificationConfig<DownloadBatchStatus> notificationConfig) {
        this.notificationConfig = notificationConfig;
        return this;
    }

    public DownloadManagerBuilder withAllowedConnectionType(ConnectionType connectionTypeNotAllowed) {
        this.connectionTypeAllowed = connectionTypeNotAllowed;
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

    public DownloadManager build() {
        Intent intent = new Intent(applicationContext, LiteDownloadService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LiteDownloadService.DownloadServiceBinder binder = (LiteDownloadService.DownloadServiceBinder) service;
                downloadService = binder.getService();
                downloadManager.submitAllStoredDownloads(new AllStoredDownloadsSubmittedCallback() {
                    @Override
                    public void onAllDownloadsSubmitted() {
                        downloadManager.initialise(downloadService);

                        if (allowNetworkRecovery) {
                            DownloadsNetworkRecoveryCreator.createEnabled(applicationContext, downloadManager, connectionTypeAllowed);
                        } else {
                            DownloadsNetworkRecoveryCreator.createDisabled();
                        }
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // no-op
            }
        };

        applicationContext.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

        FileOperations fileOperations = new FileOperations(filePersistenceCreator, fileSizeRequester, fileDownloader);
        ArrayList<DownloadBatchCallback> callbacks = new ArrayList<>();

        CallbackThrottleCreator callbackThrottleCreator = getCallbackThrottleCreator(
                callbackThrottleCreatorType,
                timeUnit,
                frequency,
                customCallbackThrottle
        );

        Executor executor = Executors.newSingleThreadExecutor();
        DownloadsFilePersistence downloadsFilePersistence = new DownloadsFilePersistence(downloadsPersistence);
        DownloadsBatchPersistence downloadsBatchPersistence = new DownloadsBatchPersistence(
                executor,
                downloadsFilePersistence,
                downloadsPersistence,
                callbackThrottleCreator
        );

        Optional<NotificationChannel> notificationChannel = notificationConfig.createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationChannel.isPresent()) {
            NotificationManager notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel.get());
        }

        LiteDownloadManagerDownloader downloader = new LiteDownloadManagerDownloader(
                LOCK,
                EXECUTOR,
                callbackHandler,
                fileOperations,
                downloadsBatchPersistence,
                downloadsFilePersistence,
                notificationConfig,
                notificationConfig,
                callbacks,
                callbackThrottleCreator
        );

        downloadManager = new DownloadManager(
                LOCK,
                EXECUTOR,
                new HashMap<DownloadBatchId, DownloadBatch>(),
                callbacks,
                fileOperations,
                downloadsBatchPersistence,
                downloader
        );

        return downloadManager;
    }

    private CallbackThrottleCreator getCallbackThrottleCreator(CallbackThrottleCreator.Type callbackThrottleType,
                                                               TimeUnit timeUnit,
                                                               long frequency,
                                                               Class<? extends CallbackThrottle> customCallbackThrottle) {
        switch (callbackThrottleType) {
            case THROTTLE_BY_TIME:
                return CallbackThrottleCreator.ByTime(timeUnit, frequency);
            case THROTTLE_BY_PROGRESS_INCREASE:
                return CallbackThrottleCreator.ByProgressIncrease();
            case CUSTOM:
                return CallbackThrottleCreator.ByCustomThrottle(customCallbackThrottle);
            default:
                throw new IllegalStateException("callbackThrottle type " + callbackThrottleType + " not implemented yet");
        }
    }
}
