package com.novoda.downloadmanager;

import java.util.Collections;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class BatchBuilderTest {

    private static final DownloadBatchId DOWNLOAD_BATCH_ID = DownloadBatchIdCreator.createFrom("download_batch_id");
    private static final String DOWNLOAD_BATCH_TITLE = "download_batch_title";
    private static final DownloadFileId DOWNLOAD_FILE_ID = new LiteDownloadFileId("download_file_id");
    private static final FileName FILE_NAME = LiteFileName.from("file_name");
    private static final String RELATIVE_PATH = "/foo/bar/5MB.zip";

    @Test
    public void returnsBatch_whenOptionalParametersAreNotSupplied() {
        Batch batch = new Batch.Builder(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE)
                .addFile("net_address").apply()
                .build();

        File expectedFile = new File("net_address", Optional.absent(), Optional.absent(), Optional.absent());
        Batch expectedBatch = new Batch(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE, Collections.singletonList(expectedFile));

        assertThat(batch).isEqualTo(expectedBatch);
    }

    @Test
    public void returnsBatch_whenOptionalParametersAreSupplied() {
        Batch batch = new Batch.Builder(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE)
                .addFile("net_address").withDownloadFileId(DOWNLOAD_FILE_ID).withFileName(FILE_NAME).withRelativePath(RELATIVE_PATH).apply()
                .build();

        File expectedFile = new File("net_address", Optional.of(DOWNLOAD_FILE_ID), Optional.of(FILE_NAME), Optional.of(RELATIVE_PATH));
        Batch expectedBatch = new Batch(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE, Collections.singletonList(expectedFile));

        assertThat(batch).isEqualTo(expectedBatch);
    }

}
