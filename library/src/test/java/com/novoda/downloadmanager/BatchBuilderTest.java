package com.novoda.downloadmanager;

import java.util.Collections;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class BatchBuilderTest {

    private static final DownloadBatchId DOWNLOAD_BATCH_ID = DownloadBatchIdCreator.createSanitizedFrom("download_batch_id");
    private static final String DOWNLOAD_BATCH_TITLE = "download_batch_title";
    private static final DownloadFileId DOWNLOAD_FILE_ID = new LiteDownloadFileId("download_file_id");

    @Test
    public void returnsBatch_whenOptionalParametersAreNotSupplied() {
        Batch batch = Batch.with(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE)
                .downloadFrom("net_address").apply()
                .build();

        BatchFile expectedBatchFile = new BatchFile("net_address", Optional.absent(), Optional.absent());
        Batch expectedBatch = new Batch(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE, Collections.singletonList(expectedBatchFile));

        assertThat(batch).isEqualTo(expectedBatch);
    }

    @Test
    public void returnsBatch_whenOptionalParametersAreSupplied() {
        Batch batch = Batch.with(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE)
                .downloadFrom("net_address").withIdentifier(DOWNLOAD_FILE_ID).saveTo("/foo/bar/", "5MB.zip").apply()
                .build();

        BatchFile expectedBatchFile = new BatchFile("net_address", Optional.of(DOWNLOAD_FILE_ID), Optional.of("/foo/bar/5MB.zip"));
        Batch expectedBatch = new Batch(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE, Collections.singletonList(expectedBatchFile));

        assertThat(batch).isEqualTo(expectedBatch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsException_whenDuplicatedFileIDsAreSupplied() {
        Batch.with(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE)
                .downloadFrom("net_address").withIdentifier(DOWNLOAD_FILE_ID).apply()
                .downloadFrom("another_address").withIdentifier(DOWNLOAD_FILE_ID).apply()
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsException_whenDuplicatedNetworkAddressWithoutFileIDsAreSupplied() {
        Batch.with(DOWNLOAD_BATCH_ID, DOWNLOAD_BATCH_TITLE)
                .downloadFrom("net_address").apply()
                .downloadFrom("net_address").apply()
                .build();
    }

}
