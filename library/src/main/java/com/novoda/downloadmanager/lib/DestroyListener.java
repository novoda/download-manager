package com.novoda.downloadmanager.lib;

public interface DestroyListener {

    void onDownloadManagerModulesDestroyed();

    class NoOp implements DestroyListener {

        @Override
        public void onDownloadManagerModulesDestroyed() {
            //no-op
        }
    }

}
