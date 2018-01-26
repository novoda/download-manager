package com.novoda.downloadmanager;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class FilePathExtractorTest {

    private static final FileName FILE_NAME = LiteFileName.from("10MB.zip");
    private static final String BASE_PATH = "/data/data/com.novoda.downloadmanager.demo.simple/files/";

    @Test
    public void returnsFilePath_whenAssetUrlOnlyContainsFileName() {
        String assetUrl = "10MB.zip";

        FilePathExtractor.DownloadFilePath filePath = FilePathExtractor.extractFrom(BASE_PATH, assetUrl);

        String expectedAbsolutePath = "/data/data/com.novoda.downloadmanager.demo.simple/files/10MB.zip";
        assertThat(filePath).isEqualTo(new FilePathExtractor.DownloadFilePath(expectedAbsolutePath, FILE_NAME));
    }

    @Test
    public void returnsFilePath_whenAssetUrlContainsSubdirectories() {
        String assetUrl = "foo/bar/10MB.zip";

        FilePathExtractor.DownloadFilePath filePath = FilePathExtractor.extractFrom(BASE_PATH, assetUrl);

        String expectedAbsolutePath = "/data/data/com.novoda.downloadmanager.demo.simple/files/foo/bar/10MB.zip";
        assertThat(filePath).isEqualTo(new FilePathExtractor.DownloadFilePath(expectedAbsolutePath, FILE_NAME));
    }

    @Test
    public void returnsAbsolutePath_whenAssetUrlAlreadyContainsBasePath() {
        String assetUrl = BASE_PATH + "foo/bar/10MB.zip";

        FilePathExtractor.DownloadFilePath filePath = FilePathExtractor.extractFrom(BASE_PATH, assetUrl);

        String expectedAbsolutePath = "/data/data/com.novoda.downloadmanager.demo.simple/files/foo/bar/10MB.zip";
        assertThat(filePath).isEqualTo(new FilePathExtractor.DownloadFilePath(expectedAbsolutePath, FILE_NAME));
    }

}
