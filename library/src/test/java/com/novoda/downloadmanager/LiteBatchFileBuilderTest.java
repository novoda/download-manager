package com.novoda.downloadmanager;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LiteBatchFileBuilderTest {

    private static final String ANY_NETWORK_ADDRESS = "http://ak.assets.com/some-file";

    private final InternalBatchBuilder batchBuilder = mock(InternalBatchBuilder.class);
    private final StorageRoot storageRoot = mock(StorageRoot.class);
    private final DownloadBatchId downloadBatchId = mock(DownloadBatchId.class);
    private final LiteBatchFileBuilder liteBatchFileBuilder = new LiteBatchFileBuilder(
            storageRoot,
            downloadBatchId,
            ANY_NETWORK_ADDRESS
    );

    @Before
    public void setUp() {
        given(downloadBatchId.rawId()).willReturn("my-movie");
    }

    @Test
    public void concatenatesAllPaths() {
        given(storageRoot.path()).willReturn("root");

        liteBatchFileBuilder
                .withParentBuilder(batchBuilder)
                .saveTo("my/path", "my-movie.mp4")
                .apply();

        verify(batchBuilder).withFile(batchFileWithPath("root/my-movie/my/path/my-movie.mp4"));
    }

    @Test
    public void doesNotAddDuplicateSeparators() {
        given(storageRoot.path()).willReturn("/root/");
        given(downloadBatchId.rawId()).willReturn("/my-movie/");

        liteBatchFileBuilder
                .withParentBuilder(batchBuilder)
                .saveTo("/my/path/", "/my-movie.mp4")
                .apply();

        verify(batchBuilder).withFile(batchFileWithPath("/root/my-movie/my/path/my-movie.mp4"));
    }

    @Test
    public void ignoresEmptyPath() {
        given(storageRoot.path()).willReturn("root");

        liteBatchFileBuilder
                .withParentBuilder(batchBuilder)
                .saveTo("", "my-movie.mp4")
                .apply();

        verify(batchBuilder).withFile(batchFileWithPath("root/my-movie/my-movie.mp4"));
    }

    @Test
    public void ignoreSeparatorAsPath() {
        given(storageRoot.path()).willReturn("root/");

        liteBatchFileBuilder
                .withParentBuilder(batchBuilder)
                .saveTo("/", "/my-movie.mp4")
                .apply();

        verify(batchBuilder).withFile(batchFileWithPath("root/my-movie/my-movie.mp4"));
    }

    @Test
    public void doesNotIgnoreSeparatorAsRoot() {
        given(storageRoot.path()).willReturn("/");

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
