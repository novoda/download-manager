package com.novoda.downloadmanager;

import java.util.Collections;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class BatchBuilderTest {

    private static final DownloadBatchId DOWNLOAD_BATCH_ID = DownloadBatchIdCreator.createFrom("download_batch_id");
    private static final String DOWNLOAD_BATCH_TITLE = "download_batch_title";
    private static final FileName FILE_NAME = LiteFileName.from("file_name");
    private static final String RELATIVE_PATH = "/foo/bar/5MB.zip";

    @Test
    public void returnsBatch_whenOptionalParametersAreNotSupplied() {
        BatchTemp batchTemp = new BatchTemp.Builder(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE)
                .addFile("net_address").apply()
                .build();

        File expectedFile = new File("net_address", Optional.absent(), Optional.absent());
        BatchTemp expectedBatch = new BatchTemp(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE, Collections.singletonList(expectedFile));

        assertThat(batchTemp).isEqualTo(expectedBatch);
    }

    @Test
    public void returnsBatch_whenOptionalParametersAreSupplied() {
        BatchTemp batchTemp = new BatchTemp.Builder(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE)
                .addFile("net_address").withFileName(FILE_NAME).withRelativePath(RELATIVE_PATH).apply()
                .build();

        File expectedFile = new File("net_address", Optional.of(FILE_NAME), Optional.of(RELATIVE_PATH));
        BatchTemp expectedBatch = new BatchTemp(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE, Collections.singletonList(expectedFile));

        assertThat(batchTemp).isEqualTo(expectedBatch);
    }

}
