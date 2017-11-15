package com.novoda.downloadmanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;

import static org.mockito.Mockito.mock;

public class DownloadBatchTest {

    private static final DownloadFile DOWNLOAD_FILE = DownloadFileFixtures.aDownloadFile().build();
    private static final Long DOWNLOAD_FILE_BYTES_DOWNLOADED = 1000L;
    private static final DownloadBatchTitle DOWNLOAD_BATCH_TITLE = DownloadBatchTitleFixtures.aDownloadBatchTitle().build();
    private static final DownloadBatchId DOWNLOAD_BATCH_ID = DownloadBatchIdFixtures.aDownloadBatchId().build();
    private static final List<DownloadFile> DOWNLOAD_FILES = Collections.singletonList(DOWNLOAD_FILE);
    private static final InternalDownloadBatchStatus INTERNAL_DOWNLOAD_BATCH_STATUS = InternalDownloadBatchStatusFixtures.anInternalDownloadsBatchStatus().build();
    private final DownloadsBatchPersistence downloadsBatchPersistence = mock(DownloadsBatchPersistence.class);
    private final CallbackThrottle callbackThrottle = mock(CallbackThrottle.class);

    @Before
    public void setUp() {
        Map<DownloadFileId, Long> bytesDownloaded = new HashMap<>();
        bytesDownloaded.put(DOWNLOAD_FILE.id(), DOWNLOAD_FILE_BYTES_DOWNLOADED);

        DownloadBatch downloadBatch = new DownloadBatch(
                DOWNLOAD_BATCH_TITLE,
                DOWNLOAD_BATCH_ID,
                DOWNLOAD_FILES,
                bytesDownloaded,
                INTERNAL_DOWNLOAD_BATCH_STATUS,
                downloadsBatchPersistence,
                callbackThrottle
        );
    }

}
