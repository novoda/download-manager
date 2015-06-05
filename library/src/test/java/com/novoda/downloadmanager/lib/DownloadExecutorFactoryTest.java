package com.novoda.downloadmanager.lib;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DownloadExecutorFactoryTest {

    @Mock
    ConcurrentDownloadsLimitProvider metadataReader;
    DownloadExecutorFactory factory;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        factory = new DownloadExecutorFactory(metadataReader);
    }

    @Test
    public void givenALimitProviderWhenTheExecutorIsCreatedThenTheCorrectLimitIsUsed() throws Exception {
        int expectedLimit = 3;
        givenALimitOf(expectedLimit);

        ThreadPoolExecutor executor = factory.createExecutor();

        assertThat(executor.getMaximumPoolSize()).isEqualTo(expectedLimit);
    }

    private void givenALimitOf(int expected) {
        when(metadataReader.getConcurrentDownloadsLimit()).thenReturn(expected);
    }

}
