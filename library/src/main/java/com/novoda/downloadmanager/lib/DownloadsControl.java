package com.novoda.downloadmanager.lib;

final class DownloadsControl {

    /**
     * This download is allowed to run.
     */
    public static final int CONTROL_RUN = 0;
    /**
     * This download must pause at the first opportunity.
     */
    public static final int CONTROL_PAUSED = 1;

    private DownloadsControl() {
        // non-instantiable class
    }

}
