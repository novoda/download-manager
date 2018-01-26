package com.novoda.downloadmanager;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class FileNameExtractorTest {

    @Test
    public void returnsFileName_whenAssetUrlOnlyContainsFileName() {
        String assetUrl = "10MB.zip";

        FileName fileName = FileNameExtractor.extractFrom(assetUrl);

        assertThat(fileName).isEqualTo(LiteFileName.from("10MB.zip"));
    }

    @Test
    public void returnsFileName_whenAssetUrlContainsSubdirectories() {
        String assetUrl = "foo/bar/10MB.zip";

        FileName fileName = FileNameExtractor.extractFrom(assetUrl);

        assertThat(fileName).isEqualTo(LiteFileName.from("10MB.zip"));
    }

}
