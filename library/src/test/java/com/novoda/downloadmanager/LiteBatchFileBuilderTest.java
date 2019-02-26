package com.novoda.downloadmanager;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LiteBatchFileBuilderTest {

    private static final String ANY_NETWORK_ADDRESS = "http://ak.assets.com/some-file";

    private final InternalBatchBuilder batchBuilder = mock(InternalBatchBuilder.class);
    private final StorageRoot storageRoot = mock(StorageRoot.class);
    private final DownloadBatchId downloadBatchId = mock(DownloadBatchId.class);

    private LiteBatchFileBuilder liteBatchFileBuilder;

    @Before
    public void setUp() {
        when(downloadBatchId.rawId()).thenReturn("my-movie");
        liteBatchFileBuilder = new LiteBatchFileBuilder(
                storageRoot,
                downloadBatchId,
                ANY_NETWORK_ADDRESS
        );
    }

    @Test
    public void concatenatesAllPaths() {
        when(storageRoot.path()).thenReturn("root");

        liteBatchFileBuilder
                .withParentBuilder(batchBuilder)
                .saveTo("my/path", "my-movie.mp4")
                .apply();

        verify(batchBuilder).withFile(batchFileWithPath("root/my-movie/my/path/my-movie.mp4"));
    }

    @Test
    public void doesNotAddDuplicatePathSeparators() {
        when(storageRoot.path()).thenReturn("root/");
        when(downloadBatchId.rawId()).thenReturn("/my-movie/");

        liteBatchFileBuilder
                .withParentBuilder(batchBuilder)
                .saveTo("/my/path/", "/my-movie.mp4")
                .apply();

        verify(batchBuilder).withFile(batchFileWithPath("root/my-movie/my/path/my-movie.mp4"));
    }

    @Test
    public void ignoresEmptyPath() {
        when(storageRoot.path()).thenReturn("root");

        liteBatchFileBuilder
                .withParentBuilder(batchBuilder)
                .saveTo("", "my-movie.mp4")
                .apply();

        verify(batchBuilder).withFile(batchFileWithPath("root/my-movie/my-movie.mp4"));
    }

    @Test
    public void ignoreSeparatorAsPath() {
        when(storageRoot.path()).thenReturn("root/");

        liteBatchFileBuilder
                .withParentBuilder(batchBuilder)
                .saveTo("/", "/my-movie.mp4")
                .apply();

        verify(batchBuilder).withFile(batchFileWithPath("root/my-movie/my-movie.mp4"));
    }

    @Test
    public void doesNotIgnoreSeparatorAsRoot() {
        when(storageRoot.path()).thenReturn("/");

        liteBatchFileBuilder
                .withParentBuilder(batchBuilder)
                .saveTo("my/path", "/my-movie.mp4")
                .apply();

        verify(batchBuilder).withFile(batchFileWithPath("/my-movie/my/path/my-movie.mp4"));
    }

    private BatchFile batchFileWithPath(String path) {
        return new BatchFile(
                ANY_NETWORK_ADDRESS,
                Optional.absent(),
                path
        );
    }

}
