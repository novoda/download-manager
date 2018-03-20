package com.novoda.downloadmanager;

/**
 * This interface can be used to create new logging strategies which
 * can then be attached to {@link DownloadManagerBuilder#withLogHandle(LogHandle)}.
 */
@SuppressWarnings("PMD.ShortMethodName")
public interface LogHandle {

    void v(Object... message);

    void i(Object... message);

    void d(Object... message);

    void d(Throwable throwable, Object... message);

    void w(Object... message);

    void w(Throwable throwable, Object... message);

    void e(Object... message);

    void e(Throwable throwable, Object... message);
}
