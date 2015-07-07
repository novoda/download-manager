/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.NetworkInfo;

interface SystemFacade {
    /**
     * @see System#currentTimeMillis()
     */
    long currentTimeMillis();

    /**
     * @return Currently active network, or null if there's no active
     * connection.
     */
    NetworkInfo getActiveNetworkInfo(int uid);

    /**
     * @return Currently active network, or null if there's no active
     * connection.
     */
    NetworkInfo getActiveNetworkInfo();

    boolean isActiveNetworkMetered();

    /**
     * @see android.telephony.TelephonyManager#isNetworkRoaming
     */
    boolean isNetworkRoaming();

    /**
     * @return maximum size, in bytes, of downloads that may go over a mobile connection; or null if
     * there's no limit
     */
    Long getMaxBytesOverMobile();

    /**
     * @return recommended maximum size, in bytes, of downloads that may go over a mobile
     * connection; or null if there's no recommended limit.  The user will have the option to bypass
     * this limit.
     */
    Long getRecommendedMaxBytesOverMobile();

    /**
     * Send a broadcast intent.
     */
    void sendBroadcast(Intent intent);

    /**
     * Returns true if the specified UID owns the specified package name.
     */
    boolean userOwnsPackage(int uid, String pckg) throws NameNotFoundException;
}
