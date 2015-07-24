package com.novoda.downloadmanager.lib.logger;

/**
 * Wrapper around Android LLog that can be easily toggled for libraries
 */
public final class LLog {

    private static boolean INITIALISED = false;

    public static String TAG = "DownloadManager";

    private LLog() {
        // util class
    }

    public static void v(Object... msg) {
        if (shouldShowLogs()) {
            android.util.Log.v(TAG, formatString(msg));
        }
    }

    public static void i(Object... msg) {
        if (shouldShowLogs()) {
            android.util.Log.i(TAG, formatString(msg));
        }
    }

    public static void d(Object... msg) {
        if (shouldShowLogs()) {
            android.util.Log.d(TAG, formatString(msg));
        }
    }

    public static void w(Object... msg) {
        if (shouldShowLogs()) {
            android.util.Log.w(TAG, formatString(msg));
        }
    }

    public static void e(Object... msg) {
        if (shouldShowLogs()) {
            android.util.Log.e(TAG, formatString(msg));
        }
    }

    public static void w(Throwable t, Object... msg) {
        if (shouldShowLogs()) {
            android.util.Log.w(TAG, formatString(msg), t);
        }
    }

    public static void d(Throwable t, Object... msg) {
        if (shouldShowLogs()) {
            android.util.Log.d(TAG, formatString(msg), t);
        }
    }

    public static void e(Throwable t, Object... msg) {
        if (shouldShowLogs()) {
            android.util.Log.e(TAG, formatString(msg), t);
        }
    }

    public static void wtf(Throwable t, Object... msg) {
        if (shouldShowLogs()) {
            android.util.Log.wtf(TAG, formatString(msg), t);
        }
    }

    private static String formatString(Object... msg) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : msg) {
            String separator = " ";
            stringBuilder.append(String.valueOf(o)).append(separator);
        }
        return stringBuilder.toString();
    }

    public static void setShowLogs(boolean showLogs) {
        LLog.INITIALISED = showLogs;
    }

    public static boolean shouldShowLogs() {
        return INITIALISED;
    }
}
