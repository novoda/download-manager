package com.novoda.downloadmanager.demo;

import android.util.Log;

import com.novoda.downloadmanager.LogHandle;

@SuppressWarnings("PMD.ShortMethodName")
public class DemoLogHandle implements LogHandle {

    private static final String TAG = "DownloadManagerDemo";
    private static final String SEPARATOR = " ";

    @Override
    public void v(Object... message) {
        Log.v(TAG, formatString(message));
    }

    @Override
    public void i(Object... message) {
        Log.i(TAG, formatString(message));
    }

    @Override
    public void d(Object... message) {
        Log.d(TAG, formatString(message));
    }

    @Override
    public void d(Throwable throwable, Object... message) {
        Log.d(TAG, formatString(message), throwable);
    }

    @Override
    public void w(Object... message) {
        Log.w(TAG, formatString(message));
    }

    @Override
    public void w(Throwable throwable, Object... message) {
        Log.w(TAG, formatString(message), throwable);
    }

    @Override
    public void e(Object... message) {
        Log.e(TAG, formatString(message));
    }

    @Override
    public void e(Throwable throwable, Object... message) {
        Log.e(TAG, formatString(message), throwable);
    }

    private static String formatString(Object... msg) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : msg) {
            stringBuilder.append(String.valueOf(o)).append(SEPARATOR);
        }
        return stringBuilder.toString();
    }
}
