package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class provides a mechanism for adding a variety
 * of {@link LogHandle} that when logging will log to all handles.
 */
@SuppressWarnings("PMD.ShortMethodName")
final class Logger {

    private static final int DOT_CLASS = 5;
    private static final int STACK_DEPTH = 4;
    private static final List<LogHandle> HANDLES = new ArrayList<>();

    private Logger() {
        // Uses static methods.
    }

    /**
     * Adds a given {@link LogHandle} to the internal list of LogHandles.
     *
     * @param handle LogHandle to log to.
     */
    static void attach(LogHandle handle) {
        HANDLES.add(handle);
    }

    /**
     * Removes a given {@link LogHandle} from the internal list of LogHandles.
     *
     * @param handle LogHandle to remove.
     */
    static void detach(LogHandle handle) {
        HANDLES.remove(handle);
    }

    /**
     * Removes all {@link LogHandle} from the internal list of LogHandles.
     */
    static void detachAll() {
        HANDLES.clear();
    }

    /**
     * Calls each internally stored {@link LogHandle#v(Object...)}
     *
     * @param message to pass to each {@link LogHandle}
     */
    public static void v(Object... message) {
        for (int i = 0; i < HANDLES.size(); i++) {
            LogHandle handle = HANDLES.get(i);
            handle.v(getDetailedLog(message));
        }
    }

    /**
     * Calls each internally stored {@link LogHandle#i(Object...)}
     *
     * @param message to pass to each {@link LogHandle}
     */
    public static void i(Object... message) {
        for (int i = 0; i < HANDLES.size(); i++) {
            LogHandle handle = HANDLES.get(i);
            handle.i(getDetailedLog(message));
        }
    }

    /**
     * Calls each internally stored {@link LogHandle#d(Object...)}
     *
     * @param message to pass to each {@link LogHandle}
     */
    public static void d(Object... message) {
        for (int i = 0; i < HANDLES.size(); i++) {
            LogHandle handle = HANDLES.get(i);
            handle.d(getDetailedLog(message));
        }
    }

    /**
     * Calls each internally stored {@link LogHandle#d(Throwable, Object...)}
     *
     * @param throwable to pass to each {@link LogHandle}
     * @param message   to pass to each {@link LogHandle}
     */
    public static void d(Throwable throwable, Object... message) {
        for (int i = 0; i < HANDLES.size(); i++) {
            LogHandle handle = HANDLES.get(i);
            handle.d(throwable, getDetailedLog(message));
        }
    }

    /**
     * Calls each internally stored {@link LogHandle#w(Object...)}
     *
     * @param message to pass to each {@link LogHandle}
     */
    public static void w(Object... message) {
        for (int i = 0; i < HANDLES.size(); i++) {
            LogHandle handle = HANDLES.get(i);
            handle.w(getDetailedLog(message));
        }
    }

    /**
     * Calls each internally stored {@link LogHandle#w(Throwable, Object...)}
     *
     * @param throwable to pass to each {@link LogHandle}
     * @param message   to pass to each {@link LogHandle}
     */
    public static void w(Throwable throwable, Object... message) {
        for (int i = 0; i < HANDLES.size(); i++) {
            LogHandle handle = HANDLES.get(i);
            handle.w(throwable, getDetailedLog(message));
        }
    }

    /**
     * Calls each internally stored {@link LogHandle#e(Object...)}
     *
     * @param message to pass to each {@link LogHandle}
     */
    public static void e(Object... message) {
        for (int i = 0; i < HANDLES.size(); i++) {
            LogHandle handle = HANDLES.get(i);
            handle.e(getDetailedLog(message));
        }
    }

    /**
     * Calls each internally stored {@link LogHandle#e(Throwable, Object...)}
     *
     * @param throwable to pass to each {@link LogHandle}
     * @param message   to pass to each {@link LogHandle}
     */
    public static void e(Throwable throwable, Object... message) {
        for (int i = 0; i < HANDLES.size(); i++) {
            LogHandle handle = HANDLES.get(i);
            handle.e(throwable, getDetailedLog(message));
        }
    }

    private static Object[] getDetailedLog(Object... message) {
        Thread currentThread = Thread.currentThread();
        final StackTraceElement trace = currentThread.getStackTrace()[STACK_DEPTH];
        final String filename = trace.getFileName();
        final String linkableSourcePosition = String.format(
                Locale.UK,
                "(%s.java:%d)",
                filename.substring(0, filename.length() - DOT_CLASS),
                trace.getLineNumber()
        );
        final String logPrefix = String.format("[%s][%s.%s] ", currentThread.getName(), linkableSourcePosition, trace.getMethodName());
        Object[] detailedMessage = new Object[message.length + 1];
        detailedMessage[0] = logPrefix;
        System.arraycopy(message, 0, detailedMessage, 1, message.length);
        return detailedMessage;
    }

}
