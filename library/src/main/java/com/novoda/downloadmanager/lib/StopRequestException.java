/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.novoda.downloadmanager.lib;

import static com.novoda.downloadmanager.lib.DownloadsStatus.STATUS_UNHANDLED_HTTP_CODE;
import static com.novoda.downloadmanager.lib.DownloadsStatus.STATUS_UNHANDLED_REDIRECT;

/**
 * Raised to indicate that the current request should be stopped immediately.
 * <p/>
 * Note the message passed to this exception will be logged and therefore must be guaranteed
 * not to contain any PII, meaning it generally can't include any information about the request
 * URI, headers, or destination filename.
 */
class StopRequestException extends Exception {
    private final int finalStatus;

    public StopRequestException(int finalStatus, String message) {
        super(message);
        this.finalStatus = finalStatus;
    }

    public StopRequestException(int finalStatus, Throwable t) {
        super(t);
        this.finalStatus = finalStatus;
    }

    public StopRequestException(int finalStatus, String message, Throwable t) {
        super(message, t);
        this.finalStatus = finalStatus;
    }

    public int getFinalStatus() {
        return finalStatus;
    }

    public static StopRequestException throwUnhandledHttpError(int code, String message) throws StopRequestException {
        final String error = "Unhandled HTTP response: " + code + " " + message;
        if (code >= 400 && code < 600) {
            throw new StopRequestException(code, error);
        } else if (code >= 300 && code < 400) {
            throw new StopRequestException(STATUS_UNHANDLED_REDIRECT, error);
        } else {
            throw new StopRequestException(STATUS_UNHANDLED_HTTP_CODE, error);
        }
    }
}
