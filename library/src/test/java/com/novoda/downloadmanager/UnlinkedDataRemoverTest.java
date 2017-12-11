package com.novoda.downloadmanager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UnlinkedDataRemoverTest {
    private static final File file1 = mock(File.class);
    private static final File file2 = mock(File.class);
    private static final File file3 = mock(File.class); // Orphan

    @Test
    public void givenUnlinkedFilesInLocalStorage_whenRemoving_thenUnlinkedFilesAreDeleted() throws Exception {
        LocalFilesDirectory localFilesDirectory = new MyLocalFilesDirectory(Arrays.asList(file1, file2, file3));
        V2DatabaseFiles v2DatabaseFiles = new MyV2DatabaseFiles(Arrays.asList(file1, file2));

        // Act
        UnlinkedDataRemover remover = new UnlinkedDataRemover(localFilesDirectory, v2DatabaseFiles);
        remover.remove();

        // Assert
        verify(file3).delete();
    }

    interface LocalFilesDirectory {
        List<File> contents();
    }

    interface V2DatabaseFiles {
        List<File> databaseContents();
    }

    private static class MyLocalFilesDirectory implements LocalFilesDirectory {
        private final List<File> fileList;

        MyLocalFilesDirectory(List<File> fileList) {
            this.fileList = fileList;
        }

        @Override
        public List<File> contents() {
            return fileList;
        }
    }

    private static class MyV2DatabaseFiles implements V2DatabaseFiles {
        private final List<File> fileList;

        private MyV2DatabaseFiles(List<File> fileList) {
            this.fileList = fileList;
        }

        @Override
        public List<File> databaseContents() {
            return fileList;
        }
    }
}
