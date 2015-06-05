package com.novoda.downloadmanager.lib;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConcurrentDownloadsLimitProviderTest {

    private static final String PACKAGE_NAME = "PACKAGE_NAME";
    private static final String METADATA_MAX_CONCURRENT_DOWNLOADS = "com.novoda.downloadmanager.MaxConcurrentDownloads";

    @Mock
    PackageManager packageManager;
    @Mock
    Bundle bundle;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void givenANullMetadataBundleWhenTheLimitIsRetrievedThenItIsTheDefaultValue() throws Exception {
        when(packageManager.getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA)).thenReturn(new StubApplicationInfo(null));
        ConcurrentDownloadsLimitProvider provider = new ConcurrentDownloadsLimitProvider(packageManager, PACKAGE_NAME);

        int concurrentDownloadsLimit = provider.getConcurrentDownloadsLimit();

        assertThat(concurrentDownloadsLimit).isEqualTo(5);
    }

    @Test
    public void givenAPackageNameWhichIsNotFoundWhenTheLimitIsRetrievedThenTheDefaultValueIsUsed() throws Exception {
        when(packageManager.getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA)).thenThrow(new PackageManager.NameNotFoundException());
        ConcurrentDownloadsLimitProvider provider = new ConcurrentDownloadsLimitProvider(packageManager, PACKAGE_NAME);

        int concurrentDownloadsLimit = provider.getConcurrentDownloadsLimit();

        assertThat(concurrentDownloadsLimit).isEqualTo(5);
    }

    @Test
    public void givenANonNullBundleWhenTheLimitIsRetrievedThenTheValueFromTheBundleIsUsed() throws Exception {
        int expected = 8;
        when(bundle.getInt(eq(METADATA_MAX_CONCURRENT_DOWNLOADS), anyInt())).thenReturn(expected);
        when(packageManager.getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA)).thenReturn(new StubApplicationInfo(bundle));
        ConcurrentDownloadsLimitProvider provider = new ConcurrentDownloadsLimitProvider(packageManager, PACKAGE_NAME);

        int concurrentDownloadsLimit = provider.getConcurrentDownloadsLimit();

        assertThat(concurrentDownloadsLimit).isEqualTo(expected);
    }

    static class StubApplicationInfo extends ApplicationInfo {
        StubApplicationInfo(Bundle metaData) {
            this.metaData = metaData;
        }
    }

}
