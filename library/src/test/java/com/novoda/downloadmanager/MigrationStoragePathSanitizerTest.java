package com.novoda.downloadmanager;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class MigrationStoragePathSanitizerTest {

    @Test
    public void stripsScheme_whenOriginalPathContainsFileScheme() {
        String originalPath = "file:/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/penguins.dat";

        String sanitizedPath = MigrationStoragePathSanitizer.sanitize(originalPath);

        assertThat(sanitizedPath).isEqualTo("/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/penguins.dat");
    }

    @Test
    public void returnsOriginalPath_whenFileSchemeIsNotPresent() {
        String originalPath = "/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/penguins.dat";

        String sanitizedPath = MigrationStoragePathSanitizer.sanitize(originalPath);

        assertThat(sanitizedPath).isEqualTo("/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/penguins.dat");
    }
}
