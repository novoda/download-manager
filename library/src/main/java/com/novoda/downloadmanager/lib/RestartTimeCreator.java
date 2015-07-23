package com.novoda.downloadmanager.lib;

class RestartTimeCreator {

    private final RandomNumberGenerator randomNumberGenerator;

    public RestartTimeCreator(RandomNumberGenerator randomNumberGenerator) {
        this.randomNumberGenerator = randomNumberGenerator;
    }

    /**
     * Returns the time when a download should be restarted.
     */
    public long restartTime(FileDownloadInfo fileDownloadInfo, long now) {
        int numFailed = fileDownloadInfo.getNumFailed();
        if (numFailed == 0) {
            return now;
        }
        int retryAfter = fileDownloadInfo.getRetryAfter();
        long lastMod = fileDownloadInfo.getLastModification();

        if (retryAfter > 0) {
            return lastMod + retryAfter;
        }
        return lastMod + Constants.RETRY_FIRST_DELAY * (1000 + randomNumberGenerator.generate()) * (1 << (numFailed - 1));
    }

}
