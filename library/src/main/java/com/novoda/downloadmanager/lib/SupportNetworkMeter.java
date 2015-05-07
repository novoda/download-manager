package com.novoda.downloadmanager.lib;

class SupportNetworkMeter implements NetworkMeter {
    @Override
    public boolean isActiveNetworkMetered() {
        return false;
    }
}
