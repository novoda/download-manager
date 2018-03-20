package com.novoda.downloadmanager;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class MigrationPathExtractorTest {

    private static final String BASE_PATH = "/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/";
    private static final DownloadBatchId DOWNLOAD_BATCH_ID = DownloadBatchIdCreator.createFrom("batch_01");

    @Test
    public void returnsAbsolutePathAndFileName_whenAssetPathConsistsOfFileNameOnly() {
        String assetUrl = "/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/thechase.dat";

        FilePath migrationPath = MigrationPathExtractor.extractMigrationPath(BASE_PATH, assetUrl, DOWNLOAD_BATCH_ID);

        assertThat(migrationPath).isEqualTo(new LiteFilePath("/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/batch_01/thechase.dat"));
    }

    @Test
    public void returnsAbsolutePathAndFileName_whenAssetPathContainsSingleSubdirectory() {
        String assetUrl = "/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/foo/thechase.dat";

        FilePath migrationPath = MigrationPathExtractor.extractMigrationPath(BASE_PATH, assetUrl, DOWNLOAD_BATCH_ID);

        assertThat(migrationPath).isEqualTo(new LiteFilePath("/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/batch_01/foo/thechase.dat"));
    }

    @Test
    public void returnsAbsolutePathAndFileName_whenAssetPathContainsMultipleSubdirectories() {
        String assetUrl = "/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/foo/bar/thechase.dat";

        FilePath migrationPath = MigrationPathExtractor.extractMigrationPath(BASE_PATH, assetUrl, DOWNLOAD_BATCH_ID);

        assertThat(migrationPath).isEqualTo(new LiteFilePath("/data/data/com.novoda.downloadmanager.demo.simple/files/Pictures/batch_01/foo/bar/thechase.dat"));
    }

}
