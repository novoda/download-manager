package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class UnlinkedDataRemoverTest {

    private static final String LINKED_FIRST_FILE = "a filename";
    private static final String LINKED_SECOND_FILE = "another filename";
    private static final String UNLINKED_THIRD_FILE = "yet another filename";

    @Test
    public void removeUnlinkedFiles_whenDataRemoverCalled() throws Exception {
        LocalFilesDirectory localFilesDirectory = new FakeLocalFilesDirectory(Arrays.asList(LINKED_FIRST_FILE, LINKED_SECOND_FILE, UNLINKED_THIRD_FILE));
        UnlinkedDataRemover remover = new UnlinkedDataRemover(new FakeV2DatabaseFiles(Arrays.asList(LINKED_FIRST_FILE, LINKED_SECOND_FILE)), localFilesDirectory);
        remover.remove();

        List<String> expectedContents = Arrays.asList(LINKED_FIRST_FILE, LINKED_SECOND_FILE);
        List<String> actualContents = localFilesDirectory.contents();

        assertThat(actualContents).isEqualTo(expectedContents);
    }

    private static class FakeLocalFilesDirectory implements LocalFilesDirectory {

        private List<String> fileList;

        FakeLocalFilesDirectory(List<String> fileList) {
            this.fileList = new ArrayList<>(fileList);
        }

        @Override
        public List<String> contents() {
            return fileList;
        }

        @Override
        public boolean deleteFile(String filename) {
            boolean fileWasRemoved = false;
            List<String> newFileList = new ArrayList<>();
            for (String s : fileList) {
                if (filename.equals(s)) {
                    fileWasRemoved = true;
                } else {
                    newFileList.add(s);
                }
            }
            fileList = newFileList;
            return fileWasRemoved;
        }
    }

    private static class FakeV2DatabaseFiles implements V2DatabaseFiles {
        
        private final List<String> fileList;

        private FakeV2DatabaseFiles(List<String> fileList) {
            this.fileList = fileList;
        }

        @Override
        public List<String> fileNames() {
            return fileList;
        }
    }
}
