package com.novoda.downloadmanager;

interface SpaceVerifier {

    void verifySpacePreemptively(int count);

    void verifySpace(int count);

}
