package com.novoda.downloadmanager;

import android.app.Notification;
import androidx.core.app.NotificationCompat;

/**
 * Used to customize the download notifications that are shown to the user.
 * Clients of this library can create their own custom implementation and
 * pass it to {@link DownloadManagerBuilder#withNotification(NotificationCustomizer)}.
 *
 * @param <T> The payload that is used to drive the customization.
 */
public interface NotificationCustomizer<T> {

    /**
     * Returns a given {@link NotificationDisplayState} for a given {@link T} payload.
     * Different payloads may want to hide or show differing notifications.
     *
     * @param payload to determine the {@link NotificationDisplayState} from.
     * @return the {@link NotificationDisplayState} used to display notifications.
     */
    NotificationDisplayState notificationDisplayState(T payload);

    /**
     * Create a custom {@link Notification} from the given {@link NotificationCompat.Builder} and payload.
     *
     * @param builder the base {@link NotificationCompat.Builder} to build the {@link Notification} from.
     * @param payload the information to add to the {@link Notification}.
     * @return the custom {@link Notification} to display.
     */
    Notification customNotificationFrom(NotificationCompat.Builder builder, T payload);

    /**
     * Represents the different ways in which a notification can be displayed to a user.
     * <p>
     * SINGLE_PERSISTENT_NOTIFICATION - normally used for DOWNLOADING, bound to a foreground service
     * SINGLE_DISMISSIBLE_NOTIFICATION - single dismissible notification IN ADDITION to the SINGLE_PERSISTENT_NOTIFICATION for DOWNLOADING
     * STACK_NOTIFICATION_NOT_DISMISSIBLE - stack notifications but do not allow user to dismiss
     * STACK_NOTIFICATION_DISMISSIBLE - stack notifications allowing user to dismiss
     * HIDDEN_NOTIFICATION - do not display a notification
     */
    enum NotificationDisplayState {
        SINGLE_PERSISTENT_NOTIFICATION,
        SINGLE_DISMISSIBLE_NOTIFICATION,
        STACK_NOTIFICATION_NOT_DISMISSIBLE,
        STACK_NOTIFICATION_DISMISSIBLE,
        HIDDEN_NOTIFICATION
    }
}
