package com.novoda.downloadmanager.lib;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DatabaseFilenameProviderTest {

    private static final String PACKAGE_NAME = "PACKAGE_NAME";
    private static final String DATABASE_FILENAME = "com.novoda.downloadmanager.DatabaseFilename";
    private static final String DEFAULT_FILENAME = "DEFAULT_FILENAME";

    @Mock
    PackageManager packageManager;
    @Mock
    Bundle bundle;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void givenANullMetadataBundleWhenTheFilenameIsRetrievedThenTheDefaultValueIsUsed() throws Exception {
        when(packageManager.getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA)).thenReturn(new StubApplicationInfo(null));
        DatabaseFilenameProvider provider = new DatabaseFilenameProvider(packageManager, PACKAGE_NAME, DEFAULT_FILENAME);

        String filename = provider.getDatabaseFilename();

        assertThat(filename).isEqualTo(DEFAULT_FILENAME);
    }

    @Test
    public void givenAPackageNameWhichIsNotFoundWhenTheFilenameIsRetrievedThenTheDefaultValueIsUsed() throws Exception {
        when(packageManager.getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA)).thenThrow(new PackageManager.NameNotFoundException());
        DatabaseFilenameProvider provider = new DatabaseFilenameProvider(packageManager, PACKAGE_NAME, DEFAULT_FILENAME);

        String filename = provider.getDatabaseFilename();

        assertThat(filename).isEqualTo(DEFAULT_FILENAME);
    }

    @Test
    public void givenANonNullBundleWhenTheFilenameIsRetrievedThenTheValueFromTheBundleIsUsed() throws Exception {
        String expected = "my_database.db";
        when(bundle.getString(eq(DATABASE_FILENAME), anyString())).thenReturn(expected);
        when(packageManager.getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA)).thenReturn(new StubApplicationInfo(bundle));
        DatabaseFilenameProvider provider = new DatabaseFilenameProvider(packageManager, PACKAGE_NAME, DEFAULT_FILENAME);

        String filename = provider.getDatabaseFilename();

        assertThat(filename).isEqualTo(expected);
    }

    static class StubApplicationInfo extends ApplicationInfo {
        StubApplicationInfo(Bundle metaData) {
            this.metaData = metaData;
        }
    }

}
