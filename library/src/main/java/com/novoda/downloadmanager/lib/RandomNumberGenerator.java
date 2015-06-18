package com.novoda.downloadmanager.lib;

/**
 * This class generates a random value to be used as a randomized restart time
 * for the server queries
 */
class RandomNumberGenerator {
    int generate() {
        return Helpers.sRandom.nextInt(1001);
    }
}
