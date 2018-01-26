package com.novoda.downloadmanager;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class FilePathCreatorTest {

    private static final String BASE_PATH = "/data/data/com.novoda.downloadmanager.demo.simple/files/";

    @Test
    public void returnsFilePath_whenAssetUrlOnlyContainsFileName() {
        String assetUrl = "10MB.zip";

        FilePath filePath = FilePathCreator.create(BASE_PATH, assetUrl);

        String expectedAbsolutePath = "/data/data/com.novoda.downloadmanager.demo.simple/files/10MB.zip";
        assertThat(filePath).isEqualTo(new LiteFilePath(expectedAbsolutePath));
    }

    @Test
    public void returnsFilePath_whenAssetUrlContainsSubdirectories() {
        String assetUrl = "foo/bar/10MB.zip";

        FilePath filePath = FilePathCreator.create(BASE_PATH, assetUrl);

        String expectedAbsolutePath = "/data/data/com.novoda.downloadmanager.demo.simple/files/foo/bar/10MB.zip";
        assertThat(filePath).isEqualTo(new LiteFilePath(expectedAbsolutePath));
    }

    @Test
    public void returnsFilePath_whenAssetUrlAlreadyContainsBasePath() {
        String assetUrl = BASE_PATH + "foo/bar/10MB.zip";

        FilePath filePath = FilePathCreator.create(BASE_PATH, assetUrl);

        String expectedAbsolutePath = "/data/data/com.novoda.downloadmanager.demo.simple/files/foo/bar/10MB.zip";
        assertThat(filePath).isEqualTo(new LiteFilePath(expectedAbsolutePath));
    }

}
