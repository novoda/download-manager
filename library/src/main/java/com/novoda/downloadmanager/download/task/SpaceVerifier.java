package com.novoda.downloadmanager.download.task;

interface SpaceVerifier {

    void verifySpacePreemptively(int count);

    void verifySpace(int count);

}