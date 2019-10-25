package com.novoda.downloadmanager;

import androidx.annotation.IntDef;
import androidx.core.app.NotificationManagerCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        NotificationManagerCompat.IMPORTANCE_UNSPECIFIED,
        NotificationManagerCompat.IMPORTANCE_NONE,
        NotificationManagerCompat.IMPORTANCE_MIN,
        NotificationManagerCompat.IMPORTANCE_LOW,
        NotificationManagerCompat.IMPORTANCE_DEFAULT,
        NotificationManagerCompat.IMPORTANCE_HIGH,
        NotificationManagerCompat.IMPORTANCE_MAX
})
@Retention(RetentionPolicy.SOURCE)
@interface Importance {
}
