package com.novoda.downloadmanager.lib;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.ExecutorService;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DownloadInfoTest {

    @Mock
    private Context mockContext;
    @Mock
    private SystemFacade mockSystemFacade;
    @Mock
    private StorageManager mockStorageManager;
    @Mock
    private DownloadNotifier mockNotifier;
    @Mock
    private ExecutorService mockExecutorService;
    @Mock
    private RandomNumberGenerator mockRandomNumberGenerator;
    @Mock
    private ContentResolver mockContentResolver;

    private static final DownloadClientReadyChecker IS_READY = new DownloadClientReadyChecker() {
        @Override
        public boolean isReadyToDownload() {
            return true;
        }
    };

    private static final DownloadClientReadyChecker IS_NOT_READY = new DownloadClientReadyChecker() {
        @Override
        public boolean isReadyToDownload() {
            return false;
        }
    };

    @Before
    public void setUp() {
        initMocks(this);

        when(mockRandomNumberGenerator.getFuzz()).thenReturn(1);
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);
        when(mockContentResolver.update(any(Uri.class), any(ContentValues.class), any(String.class), any(String[].class))).thenReturn(0);
    }

    @Test
    public void givenClientIsReadyToDownloadThenStartDownload() {

        DownloadInfo downloadInfo = new DownloadInfo(
                mockContext,
                mockSystemFacade,
                mockStorageManager,
                mockNotifier,
                mockRandomNumberGenerator,
                IS_READY);

        boolean isReady = downloadInfo.startDownloadIfReady(mockExecutorService);
        assertThat(isReady).isTrue();
    }

    @Test
    public void givenClientIsNotReadyToDownloadThenDownloadDoesNotStart() {

        DownloadInfo downloadInfo = new DownloadInfo(
                mockContext,
                mockSystemFacade,
                mockStorageManager,
                mockNotifier,
                mockRandomNumberGenerator,
                IS_NOT_READY);

        boolean isReady = downloadInfo.startDownloadIfReady(mockExecutorService);
        assertThat(isReady).isFalse();
    }
}

