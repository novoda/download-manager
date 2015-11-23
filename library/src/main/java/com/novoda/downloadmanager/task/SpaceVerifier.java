package com.novoda.downloadmanager.task;

interface SpaceVerifier {

    void verifySpacePreemptively(int count);

    void verifySpace(int count);

}