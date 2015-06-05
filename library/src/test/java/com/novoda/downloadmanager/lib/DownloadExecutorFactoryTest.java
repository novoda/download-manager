package com.novoda.downloadmanager.lib;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

public class DownloadExecutorFactoryTest {

    @Mock
    ConcurrentDownloadsLimitProvider metadataReader;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void givenALimitProviderWhenTheExecutorIsCreatedThenTheCorrectLimitIsUsed() throws Exception {
        int expectedLimit = 8;
        DownloadExecutorFactory factory = new DownloadExecutorFactory(expectedLimit);

        ThreadPoolExecutor executor = factory.createExecutor();

        assertThat(executor.getMaximumPoolSize()).isEqualTo(expectedLimit);
    }


}
