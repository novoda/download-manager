package com.novoda.downloadmanager.lib;

public interface SpaceVerifier {

    void verifySpacePreemptively(int count) throws StopRequestException;

    void verifySpace(int count) throws StopRequestException;

}
