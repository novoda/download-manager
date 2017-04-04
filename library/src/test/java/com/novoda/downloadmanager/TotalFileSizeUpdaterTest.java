package com.novoda.downloadmanager;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TotalFileSizeUpdaterTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    DownloadDatabaseWrapper downloadDatabaseWrapper;

    @Mock
    ContentLengthFetcher contentLengthFetcher;

    TotalFileSizeUpdater sizeUpdater;

    @Before
    public void setUp() {
        sizeUpdater = new TotalFileSizeUpdater(downloadDatabaseWrapper, contentLengthFetcher);
    }

    @Test
    public void whenUpdatingMissingTotalFileSizes_thenUpdatesUsingFileSizeFromContentLengthFetcher() {
        DownloadFile downloadFile = createDownloadFile();
        long totalSize = 100l;
        given(downloadDatabaseWrapper.getFilesWithUnknownTotalSize()).willReturn(Collections.singletonList(downloadFile));
        given(contentLengthFetcher.fetchContentLengthFor(downloadFile)).willReturn(totalSize);

        sizeUpdater.updateMissingTotalFileSizes();

        verify(downloadDatabaseWrapper).updateFileSize(downloadFile, totalSize);
    }

    private DownloadFile createDownloadFile() {
        return new DownloadFile(null, 0, 0, null, null);
    }
}
