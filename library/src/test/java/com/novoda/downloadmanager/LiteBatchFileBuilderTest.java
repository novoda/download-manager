package com.novoda.downloadmanager;

import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LiteBatchFileBuilderTest {

    private static final String ANY_NETWORK_ADDRESS = "http://ak.assets.com/some-file";

    private final InternalBatchBuilder batchBuilder = mock(InternalBatchBuilder.class);
    private final BatchStorageRoot batchStorageRoot = mock(BatchStorageRoot.class);
    private final BatchFileBuilder liteBatchFileBuilder = new LiteBatchFileBuilder(
            batchStorageRoot,
            ANY_NETWORK_ADDRESS
    ).withParentBuilder(batchBuilder);

    @Test
    public void concatenatesAllPaths() {
        given(batchStorageRoot.path()).willReturn("root/my-movie");

        whenLiteBatchFileBuilderApply("my/path", "my-movie.mp4");

        verify(batchBuilder).withFile(batchFileWithPath("root/my-movie/my/path/my-movie.mp4"));
    }

    @Test
    public void doesNotAddDuplicateLeadingOrTrailingSeparators() {
        given(batchStorageRoot.path()).willReturn("/root/my-movie/");

        whenLiteBatchFileBuilderApply("/my/path/", "/my-movie.mp4");

        verify(batchBuilder).withFile(batchFileWithPath("/root/my-movie/my/path/my-movie.mp4"));
    }

    @Test
    public void doesNotAddDuplicateSeparatorsWhenInMiddleOfSegments() {
        given(batchStorageRoot.path()).willReturn("/root///to///the//path/my///movie/");

        whenLiteBatchFileBuilderApply("///my/////path/", "//my-movie.mp4");

        verify(batchBuilder).withFile(batchFileWithPath("/root/to/the/path/my/movie/my/path/my-movie.mp4"));
    }

    @Test
    public void ignoresEmptyPath() {
        given(batchStorageRoot.path()).willReturn("root/my-movie");

        whenLiteBatchFileBuilderApply("", "my-movie.mp4");

        verify(batchBuilder).withFile(batchFileWithPath("root/my-movie/my-movie.mp4"));
    }

    @Test
    public void ignoreSeparatorAsPath() {
        given(batchStorageRoot.path()).willReturn("root/my-movie");

        whenLiteBatchFileBuilderApply("/", "/my-movie.mp4");

        verify(batchBuilder).withFile(batchFileWithPath("root/my-movie/my-movie.mp4"));
    }

    @Test
    public void doesNotIgnoreSeparatorAsRoot() {
        given(batchStorageRoot.path()).willReturn("/my-movie");

        whenLiteBatchFileBuilderApply("my/path", "/my-movie.mp4");

        verify(batchBuilder).withFile(batchFileWithPath("/my-movie/my/path/my-movie.mp4"));
    }

    private void whenLiteBatchFileBuilderApply(String path, String fileName) {
        liteBatchFileBuilder.saveTo(path, fileName).apply();
    }

    private BatchFile batchFileWithPath(String path) {
        return new BatchFile(
                ANY_NETWORK_ADDRESS,
                path,
                Optional.absent(),
                Optional.absent()
        );
    }

}
